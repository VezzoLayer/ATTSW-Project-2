package com.ecommerce.manager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

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
		User savedUser = new User(null, "username", "name", "email", 1000);
		userRepository.save(savedUser);

		Response response = given().contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(new Order(null, Item.BOX1, 500, savedUser)).when().post("/api/orders/new");

		Order savedOrder = response.getBody().as(Order.class);

		assertThat(orderRepository.findById(savedOrder.getId())).contains(savedOrder);
		assertThat(userRepository.findById(savedUser.getId()).orElseThrow().getBalance()).isEqualTo(500);
	}
}
