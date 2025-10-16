package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserRestController.class)
public class UserRestControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void testAllUsersEmpty() throws Exception {
		this.mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	public void testAllUsersWhenThereIsSome() throws Exception {
		when(userService.getAllUsers()).thenReturn(
				asList(new User(1L, "user 1", "test", "test", 3000), new User(2L, "user 2", "test", "test", 4000)));

		this.mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1))).andExpect(jsonPath("$[0].username", is("user 1")))
				.andExpect(jsonPath("$[0].name", is("test"))).andExpect(jsonPath("$[0].email", is("test")))
				.andExpect(jsonPath("$[0].balance", is(3000))).andExpect(jsonPath("$[1].id", is(2)))
				.andExpect(jsonPath("$[1].username", is("user 2"))).andExpect(jsonPath("$[1].name", is("test")))
				.andExpect(jsonPath("$[1].email", is("test"))).andExpect(jsonPath("$[1].balance", is(4000)));
	}

	@Test
	public void testAllOrdersEmpty() throws Exception {
		this.mvc.perform(get("/api/orders").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	public void testAllOrdersWhenThereIsSome() throws Exception {
		User user = new User(1L, "user 1", "test", "test", 3000);

		when(userService.getAllOrders())
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
	public void testOneUserByIdWithExistingUser() throws Exception {
		when(userService.getUserById(anyLong())).thenReturn(new User(1L, "user 1", "test", "test", 3000));

		this.mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.username", is("user 1")))
				.andExpect(jsonPath("$.name", is("test"))).andExpect(jsonPath("$.email", is("test")))
				.andExpect(jsonPath("$.balance", is(3000)));
	}

	@Test
	public void testOneUserByIdWithNotFoundUser() throws Exception {
		when(userService.getUserById(anyLong())).thenReturn(null);

		this.mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	@Test
	public void testOneOrderByIdWithExistingOrder() throws Exception {
		when(userService.getOrderById(anyLong()))
				.thenReturn(new Order(1L, Item.BOX1, 800, new User(1L, "user 1", "test", "test", 3000)));

		this.mvc.perform(get("/api/orders/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.item", is("BOX1")))
				.andExpect(jsonPath("$.price", is(800))).andExpect(jsonPath("$.user.username", is("user 1")))
				.andExpect(jsonPath("$.user.name", is("test"))).andExpect(jsonPath("$.user.email", is("test")))
				.andExpect(jsonPath("$.user.balance", is(3000)));
	}

	@Test
	public void testOneOrderByIdWithNotFoundOrder() throws Exception {
		when(userService.getOrderById(anyLong())).thenReturn(null);

		this.mvc.perform(get("/api/orders/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	@Test
	public void testPostUser() throws Exception {
		User requestBodyUser = new User(null, "username", "new user", "email", 4000);

		when(userService.insertNewUser(requestBodyUser))
				.thenReturn(new User(1L, "username", "new user", "email", 4000));

		this.mvc.perform(post("/api/users/new").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"username\", \"name\":\"new user\", \"email\":\"email\", \"balance\":4000}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("username"))).andExpect(jsonPath("$.name", is("new user")))
				.andExpect(jsonPath("$.email", is("email"))).andExpect(jsonPath("$.balance", is(4000)));
	}

	@Test
	public void testPostOrder() throws Exception {
		User user = new User(1L, "user 1", "test", "test", 3000);

		Order requestBodyOrder = new Order(null, Item.BOX1, 800, user);

		when(userService.insertNewOrder(requestBodyOrder)).thenReturn(new Order(1L, Item.BOX1, 800, user));

		this.mvc.perform(post("/api/orders/new").contentType(MediaType.APPLICATION_JSON).content(
				"{\"item\":\"BOX1\", \"price\":800, \"user\":{\"id\":1, \"username\":\"user 1\", \"name\":\"test\", \"email\":\"test\", \"balance\":3000}}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.item", is("BOX1"))).andExpect(jsonPath("$.price", is(800)))
				.andExpect(jsonPath("$.user.id", is(1))).andExpect(jsonPath("$.user.username", is("user 1")))
				.andExpect(jsonPath("$.user.name", is("test"))).andExpect(jsonPath("$.user.email", is("test")))
				.andExpect(jsonPath("$.user.balance", is(3000)));
	}

	@Test
	public void testPostOrderWhenInsertFailsShouldReturn400() throws Exception {
		when(userService.insertNewOrder(any(Order.class)))
				.thenThrow(new IllegalStateException("Unable to insert new order"));

		this.mvc.perform(post("/api/orders/new").contentType(MediaType.APPLICATION_JSON).content(
				"{\"item\":\"BOX1\", \"price\":800, \"user\":{\"id\":1, \"username\":\"user 1\", \"name\":\"test\", \"email\":\"test\", \"balance\":3000}}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Unable to insert new order")));
	}

	@Test
	public void testUpdateUser() throws Exception {
		User requestBodyUser = new User(null, "username", "new user", "email", 4000);

		when(userService.updateUserById(1L, requestBodyUser))
				.thenReturn(new User(1L, "username", "new user", "email", 4000));

		this.mvc.perform(put("/api/users/update/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"username\", \"name\":\"new user\", \"email\":\"email\", \"balance\":4000}")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("username"))).andExpect(jsonPath("$.name", is("new user")))
				.andExpect(jsonPath("$.email", is("email"))).andExpect(jsonPath("$.balance", is(4000)));
	}

	@Test
	public void testDepositSuccessReturns204() throws Exception {
		this.mvc.perform(post("/api/users/1/deposit").contentType(MediaType.APPLICATION_JSON).content("500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
	}

	@Test
	public void testDepositWithNegativeAmountReturns400() throws Exception {
		doThrow(new IllegalArgumentException("Deposit amount cannot be negative")).when(userService).deposit(anyLong(),
				eq(-500L));

		this.mvc.perform(post("/api/users/1/deposit").contentType(MediaType.APPLICATION_JSON).content("-500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Deposit amount cannot be negative")));
	}

	@Test
	public void testDepositWithUserNotFoundReturns404() throws Exception {
		doThrow(new IllegalStateException("User not found")).when(userService).deposit(anyLong(), anyLong());

		this.mvc.perform(post("/api/users/1/deposit").contentType(MediaType.APPLICATION_JSON).content("500"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.message", is("User not found")));
	}

	@Test
	public void testWithdrawSuccessReturns204() throws Exception {
		this.mvc.perform(post("/api/users/1/withdraw").contentType(MediaType.APPLICATION_JSON).content("500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
	}

	@Test
	public void testWithdrawWithNegativeAmountReturns400() throws Exception {
		doThrow(new IllegalArgumentException("Withdraw amount cannot be negative")).when(userService)
				.withdraw(anyLong(), eq(-500L));

		this.mvc.perform(post("/api/users/1/withdraw").contentType(MediaType.APPLICATION_JSON).content("-500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Withdraw amount cannot be negative")));
	}

	@Test
	public void testWithdrawWithUserNotFoundReturns404() throws Exception {
		doThrow(new IllegalStateException("User not found")).when(userService).withdraw(anyLong(), anyLong());

		this.mvc.perform(post("/api/users/1/withdraw").contentType(MediaType.APPLICATION_JSON).content("500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", is("User not found")));
	}

	@Test
	public void testWithdrawWhenBalanceNotEnoughReturns400() throws Exception {
		doThrow(new IllegalStateException("Not enough balance to perform withdraw")).when(userService)
				.withdraw(anyLong(), anyLong());

		this.mvc.perform(post("/api/users/1/withdraw").contentType(MediaType.APPLICATION_JSON).content("500")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Not enough balance to perform withdraw")));
	}
}
