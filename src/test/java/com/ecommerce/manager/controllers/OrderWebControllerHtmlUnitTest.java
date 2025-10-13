package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.htmlunit.WebClient;
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
import com.ecommerce.manager.services.OrderService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderWebController.class)
public class OrderWebControllerHtmlUnitTest {

	@Autowired
	private WebClient webClient;

	@MockitoBean
	private OrderService orderService;

	@Test
	public void testAllOrdersPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/orders");

		assertThat(page.getTitleText()).isEqualTo("Orders");
	}

	@Test
	public void testAllOrdersPageWithNoOrders() throws Exception {
		when(orderService.getAllOrders()).thenReturn(emptyList());

		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getBody().getTextContent()).contains("No Orders");
	}

	@Test
	public void testAllOrdersPageWithOrdersShouldShowThemInATable() throws Exception {
		User user = new User(1L, "u1", "n1", "e1", 1000);

		when(orderService.getAllOrders())
				.thenReturn(asList(new Order(1L, Item.BOX1, 100, user), new Order(2L, Item.BOX2, 200, user)));

		HtmlPage page = this.webClient.getPage("/orders");

		assertThat(page.getBody().getTextContent()).doesNotContain("No Orders");

		HtmlTable table = page.getHtmlElementById("orders_table");

		String expectedTableContent = """
				Orders
				ID Item Price User
				1 BOX1 100 1
				2 BOX2 200 1""";

		// replace /t con spazi bianchi e rimuove /r
		assertThat(table.asNormalizedText().replace("\t", " ").replace("\r", "")).isEqualTo(expectedTableContent);
	}

	@Test
	public void testEditOrderPageTitle() throws Exception {
		HtmlPage page = webClient.getPage("/editOrder/1");

		assertThat(page.getTitleText()).isEqualTo("Edit Order");
	}

	@Test
	public void testEditNonExistingOrder() throws Exception {
		when(orderService.getOrderById(1L)).thenReturn(null);

		HtmlPage page = this.webClient.getPage("/editOrder/1");

		assertThat(page.getBody().getTextContent()).contains("No order found with id: 1");
	}
}
