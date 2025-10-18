package com.ecommerce.manager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.repositories.UserRepository;

import io.restassured.RestAssured;
import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EcommerceRestControllerIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@LocalServerPort
	private int port;

	@Before
	public void setup() {
		RestAssured.port = port;

		orderRepository.deleteAll();
		orderRepository.flush();

		userRepository.deleteAll();
		userRepository.flush();
	}

	@Test
	public void testNewUser() {
		Response response = given().contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(new User(null, "username", "name", "email", 1000)).when().post("/api/users/new");

		User savedUser = response.getBody().as(User.class);

		assertThat(userRepository.findById(savedUser.getId())).contains(savedUser);
	}

	@Test
	public void testUpdateUser() {
		User savedUser = userRepository.save(new User(null, "og username", "og name", "og email", 1000));

		given().contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(new User(null, "mod username", "mod name", "mod email", 2000)).when()
				.put("/api/users/update/" + savedUser.getId()).then().statusCode(200).body("id",
						equalTo(savedUser.getId().intValue()), "username", equalTo("mod username"), "name",
						equalTo("mod name"), "email", equalTo("mod email"), "balance", equalTo(2000));
	}

	@Test
	public void testNewOrder() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		Response response = given().contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(new Order(null, Item.BOX1, 500, savedUser)).when().post("/api/orders/new");

		Order savedOrder = response.getBody().as(Order.class);

		assertThat(orderRepository.findById(savedOrder.getId())).contains(savedOrder);
		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(500);
	}

	@Test
	public void testNewOrderFails() {
		User savedUser = new User(null, "username", "name", "email", 1000);
		userRepository.save(savedUser);

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(new Order(null, Item.BOX1, 10000, savedUser)).when()
				.post("/api/orders/new").then().statusCode(400).body("message", equalTo("Unable to insert new order"));
	}

	@Test
	public void testUpdateOrder() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));
		Order savedOrder = orderRepository.save(new Order(null, Item.BOX1, 500, savedUser));

		// Per user mi limito a controllare id e balance
		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(new Order(null, Item.BOX2, 700, savedUser)).when()
				.put("/api/orders/update/" + savedOrder.getId()).then().statusCode(200).body("id",
						equalTo(savedOrder.getId().intValue()), "item", equalTo("BOX2"), "price", equalTo(700),
						"user.id", equalTo(savedUser.getId().intValue()), "user.balance", equalTo(800));
	}

	@Test
	public void testUpdateOrderFails() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));
		Order savedOrder = orderRepository.save(new Order(null, Item.BOX1, 500, savedUser));

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(new Order(null, Item.BOX2, 10000, savedUser)).when()
				.put("/api/orders/update/" + savedOrder.getId()).then().statusCode(400)
				.body("message", equalTo("Unable to update the order"));
	}

	@Test
	public void testDepositSuccess() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(500L).when()
				.post("/api/users/" + savedUser.getId() + "/deposit").then().statusCode(204);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(1500);
	}

	@Test
	public void testWithdrawSuccess() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(500L).when()
				.post("/api/users/" + savedUser.getId() + "/withdraw").then().statusCode(204);

		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(500);
	}

	@Test
	public void testWithdrawFailsDueToInsufficientBalance() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(2000L).when()
				.post("/api/users/" + savedUser.getId() + "/withdraw").then().statusCode(400)
				.body("message", equalTo("Not enough balance to perform withdraw"));
	}

	@Test
	public void testDepositFailsUserNotFound() {
		long nonExistentId = 99L;

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(500L).when()
				.post("/api/users/" + nonExistentId + "/deposit").then().statusCode(404)
				.body("message", equalTo("User not found"));
	}

	@Test
	public void testDepositFailsDueToNegativeAmount() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		given().contentType(MediaType.APPLICATION_JSON_VALUE).body(-500L).when()
				.post("/api/users/" + savedUser.getId() + "/deposit").then().statusCode(400)
				.body("message", equalTo("Deposit amount cannot be negative"));
	}

	@Test
	public void testGetOrderById() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));
		Order savedOrder = orderRepository.save(new Order(null, Item.BOX1, 500, savedUser));

		given().accept(MediaType.APPLICATION_JSON_VALUE).when().get("/api/orders/" + savedOrder.getId()).then()
				.statusCode(200).body("id", equalTo(savedOrder.getId().intValue())).body("item", equalTo("BOX1"))
				.body("price", equalTo(500)).body("user.id", equalTo(savedUser.getId().intValue()));
	}

	@Test
	public void testGetUserById() {
		User savedUser = userRepository.save(new User(null, "username", "name", "email", 1000));

		given().accept(MediaType.APPLICATION_JSON_VALUE).when().get("/api/users/" + savedUser.getId()).then()
				.statusCode(200).body("id", equalTo(savedUser.getId().intValue())).body("username", equalTo("username"))
				.body("name", equalTo("name")).body("email", equalTo("email")).body("balance", equalTo(1000));
	}

	@Test
	public void testGetAllUsers() {
		User user1 = userRepository.save(new User(null, "u1", "n1", "e1", 1000));
		User user2 = userRepository.save(new User(null, "u2", "n2", "e2", 2000));

		given().accept(MediaType.APPLICATION_JSON_VALUE).when().get("/api/users").then().statusCode(200)
				.body("size()", equalTo(2)).body("id", contains(user1.getId().intValue(), user2.getId().intValue()))
				.body("username", contains("u1", "u2")).body("name", contains("n1", "n2"))
				.body("email", contains("e1", "e2")).body("balance", contains(1000, 2000));
	}

	@Test
	public void testGetAllOrders() {
		User user1 = userRepository.save(new User(null, "u1", "n1", "e1", 1000));
		User user2 = userRepository.save(new User(null, "u2", "n2", "e2", 2000));

		Order order1 = orderRepository.save(new Order(null, Item.BOX1, 400, user1));
		Order order2 = orderRepository.save(new Order(null, Item.BOX2, 600, user2));

		given().accept(MediaType.APPLICATION_JSON_VALUE).when().get("/api/orders").then().statusCode(200)
				.body("size()", equalTo(2)).body("id", contains(order1.getId().intValue(), order2.getId().intValue()))
				.body("item", contains("BOX1", "BOX2")).body("price", contains(400, 600))
				.body("user.id", contains(user1.getId().intValue(), user2.getId().intValue()));
	}
}
