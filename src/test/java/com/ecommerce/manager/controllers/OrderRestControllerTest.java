package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.OrderService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderRestController.class)
public class OrderRestControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private OrderService orderService;

	@Test
	public void testAllOrdersEmpty() throws Exception {
		this.mvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	public void testAllOrdersWhenThereIsSome() throws Exception {
		User user = new User(1L, "user 1", "test", "test", 3000);

		when(orderService.getAllOrders())
				.thenReturn(asList(new Order(1L, Item.BOX1, 800, user), new Order(2L, Item.BOX2, 500, user)));

		this.mvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1))).andExpect(jsonPath("$[0].item", is("BOX1")))
				.andExpect(jsonPath("$[0].price", is(800))).andExpect(jsonPath("$[0].user.username", is("user 1")))
				.andExpect(jsonPath("$[0].user.name", is("test"))).andExpect(jsonPath("$[0].user.email", is("test")))
				.andExpect(jsonPath("$[0].user.balance", is(3000))).andExpect(jsonPath("$[1].id", is(2)))
				.andExpect(jsonPath("$[1].item", is("BOX2"))).andExpect(jsonPath("$[1].price", is(500)))
				.andExpect(jsonPath("$[1].user.id", is(1))).andExpect(jsonPath("$[1].user.username", is("user 1")))
				.andExpect(jsonPath("$[1].user.name", is("test"))).andExpect(jsonPath("$[1].user.email", is("test")))
				.andExpect(jsonPath("$[1].user.balance", is(3000)));
	}

	@Test
	public void testOneOrderByIdWithExistingOrder() throws Exception {
		when(orderService.getOrderById(anyLong()))
				.thenReturn(new Order(1L, Item.BOX1, 800, new User(1L, "user 1", "test", "test", 3000)));

		this.mvc.perform(get("/api/orders/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.item", is("BOX1")))
				.andExpect(jsonPath("$.price", is(800))).andExpect(jsonPath("$.user.username", is("user 1")))
				.andExpect(jsonPath("$.user.name", is("test"))).andExpect(jsonPath("$.user.email", is("test")))
				.andExpect(jsonPath("$.user.balance", is(3000)));
	}

	@Test
	public void testOneOrderByIdWithNotFoundOrder() throws Exception {
		when(orderService.getOrderById(anyLong())).thenReturn(null);

		this.mvc.perform(get("/api/orders/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	@Test
	public void testPostOrder() throws Exception {
		User user = new User(1L, "user 1", "test", "test", 3000);

		Order requestBodyOrder = new Order(null, Item.BOX1, 800, user);

		when(orderService.insertNewOrder(requestBodyOrder)).thenReturn(new Order(1L, Item.BOX1, 800, user));

		this.mvc.perform(post("/api/orders/new").contentType(MediaType.APPLICATION_JSON).content(
				"{\"item\":\"BOX1\", \"price\":800, \"user\":{\"id\":1, \"username\":\"user 1\", \"name\":\"test\", \"email\":\"test\", \"balance\":3000}}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.item", is("BOX1"))).andExpect(jsonPath("$.price", is(800)))
				.andExpect(jsonPath("$.user.id", is(1))).andExpect(jsonPath("$.user.username", is("user 1")))
				.andExpect(jsonPath("$.user.name", is("test"))).andExpect(jsonPath("$.user.email", is("test")))
				.andExpect(jsonPath("$.user.balance", is(3000)));
	}

	@Test
	public void testUpdateOrder() throws Exception {
		User user = new User(1L, "user 1", "test", "test", 3000);

		Order requestBodyOrder = new Order(null, Item.BOX1, 800, user);

		when(orderService.updateOrderById(1L, requestBodyOrder)).thenReturn(new Order(1L, Item.BOX1, 800, user));

		this.mvc.perform(put("/api/orders/update/1").contentType(MediaType.APPLICATION_JSON).content(
				"{\"item\":\"BOX1\", \"price\":800, \"user\":{\"id\":1, \"username\":\"user 1\", \"name\":\"test\", \"email\":\"test\", \"balance\":3000}}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.item", is("BOX1"))).andExpect(jsonPath("$.price", is(800)))
				.andExpect(jsonPath("$.user.id", is(1))).andExpect(jsonPath("$.user.username", is("user 1")))
				.andExpect(jsonPath("$.user.name", is("test"))).andExpect(jsonPath("$.user.email", is("test")))
				.andExpect(jsonPath("$.user.balance", is(3000)));
	}
}
