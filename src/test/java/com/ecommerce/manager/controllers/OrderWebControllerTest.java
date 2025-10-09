package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.OrderService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderWebController.class)
public class OrderWebControllerTest {

	@MockitoBean
	private OrderService orderService;

	@Autowired
	private MockMvc mvc;

	@Test
	public void testOrdersViewShowsOrdersWhenThereAreOrders() throws Exception {
		List<Order> orders = asList(new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 1000)));

		when(orderService.getAllOrders()).thenReturn(orders);

		mvc.perform(get("/orders")).andExpect(view().name("orders")).andExpect(model().attribute("orders", orders))
				.andExpect(model().attribute("message", ""));
	}
}
