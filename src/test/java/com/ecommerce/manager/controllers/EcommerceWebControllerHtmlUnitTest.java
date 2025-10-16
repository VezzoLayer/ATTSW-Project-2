package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.EcommerceService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = EcommerceWebController.class)
public class EcommerceWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;

	@MockitoBean
	private EcommerceService ecommerceService;

	@Test
	public void testHomePageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/");

		assertThat(page.getTitleText()).isEqualTo("Users");
	}

	@Test
	public void testHomePageWithNoUsers() throws Exception {
		when(ecommerceService.getAllUsers()).thenReturn(emptyList());

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getBody().getTextContent()).contains("No Users");

		// Scorro tutto il documento e cerco gli elementi 'a' che soddisfano il criterio
		assertThat(page.getByXPath("//a[text()='Show Orders']")).isEmpty();
	}

	@Test
	public void testHomePageWithUsersShouldShowThemInATable() throws Exception {
		when(ecommerceService.getAllUsers())
				.thenReturn(asList(new User(1L, "u1", "n1", "e1", 1000), new User(2L, "u2", "n2", "e2", 2000)));

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getBody().getTextContent()).doesNotContain("No Users");

		HtmlTable table = page.getHtmlElementById("users_table");

		String expectedTableContent = """
				Users
				ID Username Name Email Balance
				1 u1 n1 e1 1000 Edit Handle Balance
				2 u2 n2 e2 2000 Edit Handle Balance""";

		// replace /t con spazi bianchi e rimuove /r
		assertThat(table.asNormalizedText().replace("\t", " ").replace("\r", "")).isEqualTo(expectedTableContent);

		page.getAnchorByHref("/editUser/1");
		page.getAnchorByHref("/editUser/2");

		page.getAnchorByHref("/1/handle_balance");
		page.getAnchorByHref("/2/handle_balance");
	}

	@Test
	public void testEditUserPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/editUser/1");

		assertThat(page.getTitleText()).isEqualTo("Edit User");
	}

	@Test
	public void testEditNonExistingUser() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/editUser/1");

		assertThat(page.getBody().getTextContent()).contains("No user found with id: 1");
	}

	@Test
	public void testEditExistingUser() throws Exception {
		when(ecommerceService.getUserById(1)).thenReturn(new User(1L, "original u", "original n", "original e", 1000));

		HtmlPage page = this.webClient.getPage("/editUser/1");

		final HtmlForm form = page.getFormByName("user_form");

		form.getInputByValue("original u").setValueAttribute("modified u");
		form.getInputByValue("original n").setValueAttribute("modified n");
		form.getInputByValue("original e").setValueAttribute("modified e");
		form.getInputByValue("1000").setValueAttribute("2000");

		form.getButtonByName("btn_submit").click();

		verify(ecommerceService).updateUserById(1L, new User(1L, "modified u", "modified n", "modified e", 2000));
	}

	@Test
	public void testEditNewUser() throws Exception {
		HtmlPage page = this.webClient.getPage("/newUser");

		final HtmlForm form = page.getFormByName("user_form");

		form.getInputByName("username").setValueAttribute("new u");
		form.getInputByName("name").setValueAttribute("new n");
		form.getInputByName("email").setValueAttribute("new e");
		form.getInputByName("balance").setValueAttribute("2000");

		form.getButtonByName("btn_submit").click();

		verify(ecommerceService).insertNewUser(new User(null, "new u", "new n", "new e", 2000));
	}

	@Test
	public void testHomePageShouldHaveALinkForCreatingANewUser() throws Exception {
		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getAnchorByText("New User").getHrefAttribute()).isEqualTo("/newUser");
	}

	@Test
	public void testHandleBalancePageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/1/handle_balance");

		assertThat(page.getTitleText()).isEqualTo("Handle Balance");
	}

	@Test
	public void testHandleBalanceOfANonExistingUser() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		assertThat(page.getBody().getTextContent()).contains("No user found with id: 1");
	}

	@Test
	public void testHandleBalanceOfExistingUserShouldDisplayIdAndBalance() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		assertThat(page.getBody().getTextContent()).doesNotContain("No user found with id: 1");

		String bodyText = page.getBody().getTextContent();

		assertThat(bodyText).contains("User ID: 1").contains("Balance: 1500");
	}

	@Test
	public void testDepositWhenAmountIsAllowedShouldCallService() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("deposit_form");

		form.getInputByName("amount").setValueAttribute("500");
		form.getButtonByName("btn_deposit").click();

		verify(ecommerceService).deposit(1L, 500L);
	}

	@Test
	public void testDepositWhenIllegalArgumentExceptionIsRaisedShouldRedirectToHomeWithErrorMessage() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));
		doThrow(new IllegalArgumentException("Amount must be positive")).when(ecommerceService).deposit(1L, -500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("deposit_form");

		form.getInputByName("amount").setValueAttribute("-500");
		HtmlPage resultPage = form.getButtonByName("btn_deposit").click();

		verify(ecommerceService).deposit(1L, -500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Amount must be positive");
	}

	@Test
	public void testWithdrawWhenAmountIsAllowedShouldCallService() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("500");
		form.getButtonByName("btn_withdraw").click();

		verify(ecommerceService).withdraw(1L, 500L);
	}

	@Test
	public void testWithdrawWhenIllegalArgumentExceptionIsRaisedShouldRedirectToHomeWithErrorMessage()
			throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 1500));
		doThrow(new IllegalArgumentException("Amount must be positive")).when(ecommerceService).withdraw(1L, -500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("-500");
		HtmlPage resultPage = form.getButtonByName("btn_withdraw").click();

		verify(ecommerceService).withdraw(1L, -500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Amount must be positive");
	}

	@Test
	public void testWithdrawWhenIllegalStateExceptionIsRaisedShouldRedirectToHomeWithErrorMessage() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(new User(1L, "u1", "n1", "e1", 300));
		doThrow(new IllegalStateException("Balance is not enough")).when(ecommerceService).withdraw(1L, 500L);

		HtmlPage page = this.webClient.getPage("/1/handle_balance");

		HtmlForm form = page.getFormByName("withdraw_form");

		form.getInputByName("amount").setValueAttribute("500");
		HtmlPage resultPage = form.getButtonByName("btn_withdraw").click();

		verify(ecommerceService).withdraw(1L, 500L);
		assertThat(resultPage.getBody().getTextContent()).contains("Balance is not enough");
	}

	@Test
	public void testHomePageShouldHaveALinkForShowingOrdersWhenThereIsAtLeastOneUser() throws Exception {
		when(ecommerceService.getAllUsers()).thenReturn(asList(new User(1L, "u1", "n1", "e1", 1000)));

		HtmlPage page = this.webClient.getPage("/");

		assertThat(page.getAnchorByText("Show Orders").getHrefAttribute()).isEqualTo("/orders");
	}

	@Test
	public void testAllOrdersPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/orders");

		assertThat(page.getTitleText()).isEqualTo("Orders");
	}

	@Test
	public void testAllOrdersPageWithNoOrders() throws Exception {
		when(ecommerceService.getAllOrders()).thenReturn(emptyList());

		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getBody().getTextContent()).contains("No Orders");
	}

	@Test
	public void testAllOrdersPageWithOrdersShouldShowThemInATable() throws Exception {
		User user = new User(1L, "u1", "n1", "e1", 1000);

		when(ecommerceService.getAllOrders())
				.thenReturn(asList(new Order(1L, Item.BOX1, 100, user), new Order(2L, Item.BOX2, 200, user)));

		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getBody().getTextContent()).doesNotContain("No Orders");

		HtmlTable table = page.getHtmlElementById("orders_table");

		String expectedTableContent = """
				Orders
				ID Item Price User
				1 BOX1 100 1 Edit
				2 BOX2 200 1 Edit""";

		// replace /t con spazi bianchi e rimuove /r
		assertThat(table.asNormalizedText().replace("\t", " ").replace("\r", "")).isEqualTo(expectedTableContent);

		page.getAnchorByHref("/editOrder/1");
		page.getAnchorByHref("/editOrder/2");
	}

	@Test
	public void testEditOrderPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/editOrder/1");

		assertThat(page.getTitleText()).isEqualTo("Edit Order");
	}

	@Test
	public void testEditNonExistingOrder() throws Exception {
		when(ecommerceService.getOrderById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		assertThat(page.getBody().getTextContent()).contains("No order found with id: 1");
	}

	@Test
	public void testEditExistingOrder() throws Exception {
		User user1 = new User(1L, "u1", "n1", "e1", 1000);
		User user2 = new User(2L, "u2", "n2", "e2", 2000);

		when(ecommerceService.getOrderById(1L)).thenReturn(new Order(1L, Item.BOX1, 100, user1));

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByValue("BOX1").setValueAttribute("BOX2");
		form.getInputByValue("100").setValueAttribute("200");

		// Altrimenti catcha prima l'hidden id dell'order invece che dello user
		form.getInputByName("user.id").setValueAttribute("2");

		form.getButtonByName("btn_submit").click();

		verify(ecommerceService).updateOrderById(1L, new Order(1L, Item.BOX2, 200, user2));
	}

	@Test
	public void testEditNewOrder() throws Exception {
		HtmlPage page = this.webClient.getPage("/newOrder");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		form.getButtonByName("btn_submit").click();

		verify(ecommerceService).insertNewOrder(new Order(null, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));
	}

	@Test
	public void testAllOrderPageShouldHaveALinkForCreatingANewOrder() throws Exception {
		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getAnchorByText("New Order").getHrefAttribute()).isEqualTo("/newOrder");
	}

	@Test
	public void testAllOrdersPageShouldHaveALinkForShowingUsers() throws Exception {
		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getAnchorByText("Show Users").getHrefAttribute()).isEqualTo("/");
	}

	public void testInsertNewOrderWhenIllegalStateExceptionIsRaisedShouldRedirectToAllOrdersPageWithErrorMessage()
			throws Exception {
		doThrow(new IllegalStateException("Unable to insert new order")).when(ecommerceService)
				.insertNewOrder(any(Order.class));

		HtmlPage page = this.webClient.getPage("/newOrder");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		HtmlPage resultPage = form.getButtonByName("btn_submit").click();

		verify(ecommerceService).insertNewOrder(new Order(null, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));

		assertThat(resultPage.getBody().getTextContent()).contains("Unable to insert new order");
	}

	public void testUpdateOrderWhenIllegalStateExceptionIsRaisedShouldRedirectToAllOrdersPageWithErrorMessage()
			throws Exception {
		doThrow(new IllegalStateException("Unable to update the order")).when(ecommerceService).updateOrderById(anyLong(),
				any(Order.class));

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		HtmlPage resultPage = form.getButtonByName("btn_submit").click();

		verify(ecommerceService).updateOrderById(1L, new Order(1L, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));

		assertThat(resultPage.getBody().getTextContent()).contains("Unable to update the order");
	}
}
