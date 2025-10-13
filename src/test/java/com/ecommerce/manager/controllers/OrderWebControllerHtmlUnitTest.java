package com.ecommerce.manager.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

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
}
