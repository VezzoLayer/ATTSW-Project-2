package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.manager.model.Item;

import io.github.bonigarcia.wdm.WebDriverManager;

public class EcommerceWebControllerE2E { // NOSONAR not a standard testcase name

	private static final Logger LOGGER = LoggerFactory.getLogger(EcommerceWebControllerE2E.class);
	private static final int TIMEOUT_SECONDS = 5;

	private static int port = Integer.parseInt(System.getProperty("server.port", "8080"));

	private static String baseUrl = "http://localhost:" + port;

	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeClass
	public static void setupClass() {
		// setup Chrome Driver
		WebDriverManager.chromedriver().setup();
	}

	@Before
	public void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new ChromeDriver();
		wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
	}

	@After
	public void teardown() {
		driver.quit();
	}

	@Test
	public void testCreateNewUser() {
		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/newUser")).click();

		driver.findElement(By.name("username")).sendKeys("new username");
		driver.findElement(By.name("name")).sendKeys("new name");
		driver.findElement(By.name("email")).sendKeys("new email");
		driver.findElement(By.name("balance")).sendKeys("2000");

		driver.findElement(By.name("btn_submit")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		assertThat(driver.findElement(By.id("users_table")).getText()).contains("new username", "new name", "new email",
				"2000");
	}

	@Test
	public void testEditUser() throws JSONException {
		String id = postUser("username to edit", "name to edit", "email to edit", 1000);

		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/editUser/" + id + "']")).click();

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

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		assertThat(driver.findElement(By.id("users_table")).getText()).contains(id, "mod username", "mod name",
				"mod email", "2000");
	}

	@Test
	public void testHandleBalancePerformWithdraw() throws JSONException {
		String userId = postUser("username test withdraw", "name test withdraw", "email test withdraw", 1000);

		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/" + userId + "/handle_balance")).click();

		driver.findElement(By.id("withdraw_amount")).sendKeys("500");

		driver.findElement(By.name("btn_withdraw")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		assertThat(driver.findElement(By.id("users_table")).getText()).contains("username test withdraw",
				"name test withdraw", "email test withdraw", "500");
	}

	@Test
	public void testHandleBalancePerformDeposit() throws JSONException {
		String userId = postUser("username test deposit", "name test deposit", "email test deposit", 1000);

		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/" + userId + "/handle_balance")).click();

		driver.findElement(By.id("deposit_amount")).sendKeys("500");

		driver.findElement(By.name("btn_deposit")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		assertThat(driver.findElement(By.id("users_table")).getText()).contains("username test deposit",
				"name test deposit", "email test deposit", "1500");
	}

	private String postUser(String username, String name, String email, long balance) throws JSONException {
		JSONObject body = new JSONObject();

		body.put("username", username);
		body.put("name", name);
		body.put("email", email);
		body.put("balance", balance);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);

		ResponseEntity<String> answer = new RestTemplate().postForEntity(baseUrl + "/api/users/new", entity,
				String.class);

		LOGGER.debug("answer for POST user: {}", answer);
		return new JSONObject(answer.getBody()).get("id").toString();
	}

	@Test
	public void testCreateNewOrder() throws JSONException {
		// Mi assicuro di avere uno user per trovare l'href a /orders
		String userId = postUser("username for order", "name for order", "email for order", 1000);

		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/orders']")).click();
		driver.findElement(By.cssSelector("a[href*='/newOrder']")).click();

		driver.findElement(By.name("item")).sendKeys("BOX1");
		driver.findElement(By.name("price")).sendKeys("500");
		driver.findElement(By.name("user.id")).sendKeys(userId);

		driver.findElement(By.name("btn_submit")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("orders_table")));
		assertThat(driver.findElement(By.id("orders_table")).getText()).contains("BOX1", "500", userId);

		// Confronto anche il testo
		driver.findElement(By.xpath("//a[@href='/' and text()='Show Users']")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		// Si controlla anche che l'user abbia il saldo decrementato
		assertThat(driver.findElement(By.id("users_table")).getText()).contains(userId, "username for order",
				"name for order", "email for order", "500");
	}

	@Test
	public void testEditOrder() throws JSONException {
		String oldUserId = postUser("username edit order", "name edit order", "email edit order", 2000);
		String newUserId = postUser("username edit order2", "name edit order2", "email edit order2", 2000);
		String orderId = postOrder(Item.BOX3, 500, oldUserId);

		driver.get(baseUrl);

		driver.findElement(By.cssSelector("a[href*='/orders']")).click();
		driver.findElement(By.cssSelector("a[href*='/editOrder/" + orderId + "']")).click();

		final WebElement itemField = driver.findElement(By.name("item"));
		itemField.clear();
		itemField.sendKeys("BOX2");

		final WebElement priceField = driver.findElement(By.name("price"));
		priceField.clear();
		priceField.sendKeys("1000");

		final WebElement userIdField = driver.findElement(By.name("user.id"));
		userIdField.clear();
		userIdField.sendKeys(newUserId);

		driver.findElement(By.name("btn_submit")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("orders_table")));
		assertThat(driver.findElement(By.id("orders_table")).getText()).contains(orderId, "BOX2", "1000", newUserId);

		// Confronto anche il testo
		driver.findElement(By.xpath("//a[@href='/' and text()='Show Users']")).click();

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("users_table")));
		// Si controlla anche che l'user vecchio abbia il saldo originale
		assertThat(driver.findElement(By.id("users_table")).getText()).contains(oldUserId, "username edit order",
				"name edit order", "email edit order", "1000");
		// Si controlla anche che l'user nuovo abbia il saldo aggiornato
		assertThat(driver.findElement(By.id("users_table")).getText()).contains(newUserId, "username edit order2",
				"name edit order2", "email edit order2", "1000");
	}

	private String postOrder(Item item, long price, String userId) throws JSONException {
		JSONObject body = new JSONObject();

		JSONObject userObj = new JSONObject();
		userObj.put("id", userId);

		body.put("item", item);
		body.put("price", price);
		body.put("user", userObj);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(body.toString(), headers);

		ResponseEntity<String> answer = new RestTemplate().postForEntity(baseUrl + "/api/orders/new", entity,
				String.class);

		LOGGER.debug("answer for POST order: {}", answer);
		return new JSONObject(answer.getBody()).get("id").toString();
	}
}
