package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
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

	@Test
	public void testEditPageNewUser() {
		driver.get(baseUrl + "/newUser");

		driver.findElement(By.name("username")).sendKeys("new username");
		driver.findElement(By.name("name")).sendKeys("new name");
		driver.findElement(By.name("email")).sendKeys("new email");
		driver.findElement(By.name("balance")).sendKeys("1000");
		driver.findElement(By.name("btn_submit")).click();

		assertThat(userRepository.findByUsername("new username")).usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new User(null, "new username", "new name", "new email", 1000L));
	}

	@Test
	public void testEditPageUpdateUser() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000L));
		driver.get(baseUrl + "/editUser/" + testUser.getId());

		final WebElement usernameField = driver.findElement(By.name("username"));
		usernameField.clear();
		usernameField.sendKeys("mod username");

		final WebElement nameField = driver.findElement(By.name("name"));
		nameField.clear();
		nameField.sendKeys("mod name");

		final WebElement emailField = driver.findElement(By.name("email"));
		emailField.clear();
		emailField.sendKeys("mod email");

		final WebElement balanceField = driver.findElement(By.name("balance"));
		balanceField.clear();
		balanceField.sendKeys("2000");

		driver.findElement(By.name("btn_submit")).click();

		assertThat(userRepository.findByUsername("mod username")).usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new User(null, "mod username", "mod name", "mod email", 2000L));
	}

	@Test
	public void testEditPageUpdateUserNotFound() {
		long nonexistentUserId = 999L;
		driver.get(baseUrl + "/editUser/" + nonexistentUserId);

		String bodyText = driver.findElement(By.tagName("body")).getText();

		assertThat(bodyText).contains("No user found with id: " + nonexistentUserId);
	}

	@Test
	public void testHandleBalanceDepositSuccess() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000L));

		driver.get(baseUrl + "/" + testUser.getId() + "/handle_balance");
		driver.findElement(By.id("deposit_amount")).sendKeys("500");
		driver.findElement(By.name("btn_deposit")).click();

		assertThat(userRepository.findByUsername("t username")).usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new User(null, "t username", "t name", "t email", 1500L));
	}

	@Test
	public void testHandleBalanceDepositNegativeAmountShowsErrorOnHomePage() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000L));

		driver.get(baseUrl + "/" + testUser.getId() + "/handle_balance");
		driver.findElement(By.id("deposit_amount")).sendKeys("-500");
		driver.findElement(By.name("btn_deposit")).click();

		WebElement body = driver.findElement(By.tagName("body"));

		assertThat(body.getText()).contains("Deposit amount cannot be negative");
	}

	@Test
	public void testHandleBalanceWithdrawSuccess() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000L));

		driver.get(baseUrl + "/" + testUser.getId() + "/handle_balance");
		driver.findElement(By.id("withdraw_amount")).sendKeys("500");
		driver.findElement(By.name("btn_withdraw")).click();

		assertThat(userRepository.findByUsername("t username")).usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new User(null, "t username", "t name", "t email", 500L));
	}

	@Test
	public void testHandleBalanceWithdrawNegativeAmountShowsErrorOnHomePage() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000L));

		driver.get(baseUrl + "/" + testUser.getId() + "/handle_balance");
		driver.findElement(By.id("withdraw_amount")).sendKeys("-500");
		driver.findElement(By.name("btn_withdraw")).click();

		WebElement body = driver.findElement(By.tagName("body"));

		assertThat(body.getText()).contains("Withdraw amount cannot be negative");
	}

	@Test
	public void testHandleBalanceWithdrawTooMuchShowsErrorOnHomePage() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 100L));

		driver.get(baseUrl + "/" + testUser.getId() + "/handle_balance");
		driver.findElement(By.id("withdraw_amount")).sendKeys("500");
		driver.findElement(By.name("btn_withdraw")).click();

		WebElement body = driver.findElement(By.tagName("body"));

		assertThat(body.getText()).contains("Not enough balance to perform withdraw");
	}

	@Test
	public void testHandleBalanceUserNotFound() {
		long nonexistentUserId = 999L;

		driver.get(baseUrl + "/" + nonexistentUserId + "/handle_balance");
		String bodyText = driver.findElement(By.tagName("body")).getText();

		assertThat(bodyText).contains("No user found with id: " + nonexistentUserId);
	}

	@Test
	public void testAllOrdersPageWhenNoOrdersArePresent() {
		driver.get(baseUrl + "/orders");

		assertThat(driver.findElement(By.tagName("body")).getText()).contains("No Orders");
	}

	@Test
	public void testAllOrdersPageWhenOrdersArePresent() {
		User testUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000));
		Order testOrder = orderRepository.save(new Order(null, Item.BOX1, 500, testUser));

		driver.get(baseUrl + "/orders");

		assertThat(driver.findElement(By.id("orders_table")).getText()).contains("BOX1", "500",
				testUser.getId().toString(), "Edit");

		driver.findElement(By.cssSelector("a[href*='/editOrder/" + testOrder.getId() + "']"));
	}

	@Test
	public void testEditPageNewOrder() {
		User savedUser = userRepository.save(new User(null, "t username", "t name", "t email", 1000));

		driver.get(baseUrl + "/newOrder");

		driver.findElement(By.name("item")).sendKeys("BOX2");
		driver.findElement(By.name("price")).sendKeys("500");
		driver.findElement(By.name("user.id")).sendKeys(savedUser.getId().toString());
		driver.findElement(By.name("btn_submit")).click();

		var orders = orderRepository.findByItem(Item.BOX2);

		assertThat(orders).hasSize(1);
		assertThat(orders.get(0)).usingRecursiveComparison().ignoringFields("id", "user.id")
				.isEqualTo(new Order(null, Item.BOX2, 500, new User(null, "t username", "t name", "t email", 500)));
	}
}
