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
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserWebController.class)
public class OrderWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;

	@MockitoBean
	private UserService userService;

	@Test
	public void testAllOrdersPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/orders");

		assertThat(page.getTitleText()).isEqualTo("Orders");
	}

	@Test
	public void testAllOrdersPageWithNoOrders() throws Exception {
		when(userService.getAllOrders()).thenReturn(emptyList());

		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getBody().getTextContent()).contains("No Orders");
	}

	@Test
	public void testAllOrdersPageWithOrdersShouldShowThemInATable() throws Exception {
		User user = new User(1L, "u1", "n1", "e1", 1000);

		when(userService.getAllOrders())
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
		when(userService.getOrderById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		assertThat(page.getBody().getTextContent()).contains("No order found with id: 1");
	}

	@Test
	public void testEditExistingOrder() throws Exception {
		User user1 = new User(1L, "u1", "n1", "e1", 1000);
		User user2 = new User(2L, "u2", "n2", "e2", 2000);

		when(userService.getOrderById(1L)).thenReturn(new Order(1L, Item.BOX1, 100, user1));

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByValue("BOX1").setValueAttribute("BOX2");
		form.getInputByValue("100").setValueAttribute("200");

		// Altrimenti catcha prima l'hidden id dell'order invece che dello user
		form.getInputByName("user.id").setValueAttribute("2");

		form.getButtonByName("btn_submit").click();

		verify(userService).updateOrderById(1L, new Order(1L, Item.BOX2, 200, user2));
	}

	@Test
	public void testEditNewOrder() throws Exception {
		HtmlPage page = this.webClient.getPage("/newOrder");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		form.getButtonByName("btn_submit").click();

		verify(userService).insertNewOrder(new Order(null, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));
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
		doThrow(new IllegalStateException("Unable to insert new order")).when(userService)
				.insertNewOrder(any(Order.class));

		HtmlPage page = this.webClient.getPage("/newOrder");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		HtmlPage resultPage = form.getButtonByName("btn_submit").click();

		verify(userService).insertNewOrder(new Order(null, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));

		assertThat(resultPage.getBody().getTextContent()).contains("Unable to insert new order");
	}

	public void testUpdateOrderWhenIllegalStateExceptionIsRaisedShouldRedirectToAllOrdersPageWithErrorMessage()
			throws Exception {
		doThrow(new IllegalStateException("Unable to update the order")).when(userService).updateOrderById(anyLong(),
				any(Order.class));

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		final HtmlForm form = page.getFormByName("order_form");

		form.getInputByName("item").setValueAttribute("BOX1");
		form.getInputByName("price").setValueAttribute("100");
		form.getInputByName("user.id").setValueAttribute("1");

		HtmlPage resultPage = form.getButtonByName("btn_submit").click();

		verify(userService).updateOrderById(1L, new Order(1L, Item.BOX1, 100, new User(1L, "u", "n", "e", 1000)));

		assertThat(resultPage.getBody().getTextContent()).contains("Unable to update the order");
	}
}
