package com.ecommerce.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class EcommerceWebControllerE2E {

	private static int port = Integer.parseInt(System.getProperty("server.port", "8080"));

	private static String baseUrl = "http://localhost:" + port;

	private WebDriver driver;

	@BeforeClass
	public static void setupClass() {
		// setup Chrome Driver
		WebDriverManager.chromedriver().setup();
	}

	@Before
	public void setup() {
		baseUrl = "http://localhost:" + port;
		driver = new ChromeDriver();
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

		assertThat(driver.findElement(By.id("users_table")).getText()).contains("new username", "new name", "new email",
				"2000");
	}

}
