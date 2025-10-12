package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
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

		mvc.perform(get("/orders")).andExpect(view().name("all-orders")).andExpect(model().attribute("orders", orders))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testOrdersViewShowsMessageWhenThereAreNoOrders() throws Exception {
		when(orderService.getAllOrders()).thenReturn(Collections.emptyList());

		mvc.perform(get("/orders")).andExpect(view().name("all-orders"))
				.andExpect(model().attribute("orders", Collections.emptyList()))
				.andExpect(model().attribute("message", "No order to show"));
	}

	@Test
	public void testEditOrderWhenTheOrderIsFound() throws Exception {
		Order order = new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 1000));

		when(orderService.getOrderById(1L)).thenReturn(order);

		mvc.perform(get("/editOrder/1")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", order)).andExpect(model().attribute("message", ""));
	}

	@Test
	public void testEditOrderWhenOrderIsNotFound() throws Exception {
		when(orderService.getOrderById(1L)).thenReturn(null);

		mvc.perform(get("/editOrder/1")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", nullValue()))
				.andExpect(model().attribute("message", "No order found with id: 1"));
	}

	@Test
	public void testEditNewOrder() throws Exception {
		mvc.perform(get("/newOrder")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", new Order())).andExpect(model().attribute("message", ""));

		verifyNoMoreInteractions(orderService);
	}

	@Test
	public void testPostOrderWithoutIdShouldInsertNewOrder() throws Exception {
		mvc.perform(post("/saveOrder").param("item", "BOX1").param("price", "700").param("user.name", "test")
				.param("user.surname", "test").param("user.email", "test").param("user.balance", "2000"))
				.andExpect(redirectedUrl("/orders"));

		verify(orderService)
				.insertNewOrder(new Order(null, Item.BOX1, 700, new User(1L, "test", "test", "test", 2000)));
	}

	@Test
	public void testPostOrderWhenInsertFailsShouldHandleException() throws Exception {
		doThrow(new IllegalStateException("Unable to insert new order")).when(orderService)
				.insertNewOrder(any(Order.class));

		mvc.perform(post("/saveOrder").param("item", "BOX1").param("price", "700").param("user.name", "test")
				.param("user.username", "test").param("user.email", "test").param("user.balance", "500"))
				.andExpect(redirectedUrl("/orders"))
				.andExpect(flash().attribute("error", "Unable to insert new order"));

		verify(orderService).insertNewOrder(any(Order.class));
		verifyNoMoreInteractions(orderService);
	}

	@Test
	public void testPostOrderWithIdShouldUpdateExistingOrder() throws Exception {
		mvc.perform(post("/saveOrder").param("id", "1").param("item", "BOX1").param("price", "700")
				.param("user.name", "test").param("user.surname", "test").param("user.email", "test")
				.param("user.balance", "2000")).andExpect(redirectedUrl("/orders"));

		verify(orderService).updateOrderById(1L,
				new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 2000)));
	}

	@Test
	public void testPostOrderWhenUpdateFailsShouldHandleException() throws Exception {
		doThrow(new IllegalStateException("Unable to update the order")).when(orderService).updateOrderById(anyLong(),
				any(Order.class));

		mvc.perform(post("/saveOrder").param("id", "1").param("item", "BOX1").param("price", "700")
				.param("user.name", "test").param("user.username", "test").param("user.email", "test")
				.param("user.balance", "500")).andExpect(redirectedUrl("/orders"))
				.andExpect(flash().attribute("error", "Unable to update the order"));

		verify(orderService).updateOrderById(anyLong(), any(Order.class));
		verifyNoMoreInteractions(orderService);
	}
}
