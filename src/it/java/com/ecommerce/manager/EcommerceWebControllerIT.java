package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.repositories.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EcommerceWebControllerIT {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@LocalServerPort
	private int port;

	private WebDriver driver;

	private String baseUrl;

	@Before
	public void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new HtmlUnitDriver();

		orderRepository.deleteAll();
		orderRepository.flush();

		userRepository.deleteAll();
		userRepository.flush();
	}

	@After
	public void teardown() {
		driver.quit();
	}

	@Test
	public void testHomePageWhenNoUsersArePresent() {
		driver.get(baseUrl);

		assertThat(driver.findElement(By.tagName("body")).getText()).contains("No Users");
	}

	@Test
	public void testHomePageWhenUsersArePresent() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000));
		driver.get(baseUrl);

		assertThat(driver.findElement(By.id("users_table")).getText()).contains("t username", "t name", "t email",
				"1000", "Edit", "Handle Balance");

		driver.findElement(By.cssSelector("a[href*='/editUser/" + testUser.getId() + "']"));
		driver.findElement(By.cssSelector("a[href*='/" + testUser.getId() + "/handle_balance']"));
		driver.findElement(By.cssSelector("a[href*='/orders']"));
	}
}
