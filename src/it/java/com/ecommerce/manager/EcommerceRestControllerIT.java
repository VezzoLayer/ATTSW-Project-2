package com.ecommerce.manager;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;
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
}
