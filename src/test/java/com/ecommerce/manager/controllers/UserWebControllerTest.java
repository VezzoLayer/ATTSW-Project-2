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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.manager.model.Item;
import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.EcommerceService;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
@WebMvcTest(controllers = UserWebController.class)
public class UserWebControllerTest {

	@ClassRule
	public static final SpringClassRule springClassRule = new SpringClassRule();

	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@MockitoBean
	private EcommerceService ecommerceService;

	@Autowired
	private MockMvc mvc;

	@Test
	public void testStatus200() throws Exception {
		mvc.perform(get("/")).andExpect(status().is2xxSuccessful());
	}

	@Test
	public void testReturnHomeView() throws Exception {
		ModelAndViewAssert.assertViewName(mvc.perform(get("/")).andReturn().getModelAndView(), "index");
	}

	@Test
	public void testHomeViewShowsUsersWhenThereAreUsers() throws Exception {
		List<User> users = asList(new User(1L, "test", "test", "test", 1000));

		when(ecommerceService.getAllUsers()).thenReturn(users);

		mvc.perform(get("/")).andExpect(view().name("index")).andExpect(model().attribute("users", users))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testHomeViewShowsMessageWhenThereAreNoUsers() throws Exception {
		when(ecommerceService.getAllUsers()).thenReturn(Collections.emptyList());

		mvc.perform(get("/")).andExpect(view().name("index"))
				.andExpect(model().attribute("users", Collections.emptyList()))
				.andExpect(model().attribute("message", "No user to show"));
	}

	@Test
	public void testOrdersViewShowsOrdersWhenThereAreOrders() throws Exception {
		List<Order> orders = asList(new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 1000)));

		when(ecommerceService.getAllOrders()).thenReturn(orders);

		mvc.perform(get("/orders")).andExpect(view().name("all-orders")).andExpect(model().attribute("orders", orders))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testOrdersViewShowsMessageWhenThereAreNoOrders() throws Exception {
		when(ecommerceService.getAllOrders()).thenReturn(Collections.emptyList());

		mvc.perform(get("/orders")).andExpect(view().name("all-orders"))
				.andExpect(model().attribute("orders", Collections.emptyList()))
				.andExpect(model().attribute("message", "No order to show"));
	}

	@Test
	@Parameters({ "/editUser/1, edit-user", "/1/handle_balance, handle-balance" })
	public void testParameterizedMappingNameWhenTheUserIsFound(String mappingName, String expectedView)
			throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(ecommerceService.getUserById(1L)).thenReturn(user);

		mvc.perform(get(mappingName)).andExpect(view().name(expectedView)).andExpect(model().attribute("user", user))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testEditUserWhenUserIsNotFound() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/editUser/1")).andExpect(view().name("edit-user"))
				.andExpect(model().attribute("user", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testEditOrderWhenTheOrderIsFound() throws Exception {
		Order order = new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 1000));

		when(ecommerceService.getOrderById(1L)).thenReturn(order);

		mvc.perform(get("/editOrder/1")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", order)).andExpect(model().attribute("message", ""));
	}

	@Test
	public void testEditOrderWhenOrderIsNotFound() throws Exception {
		when(ecommerceService.getOrderById(1L)).thenReturn(null);

		mvc.perform(get("/editOrder/1")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", nullValue()))
				.andExpect(model().attribute("message", "No order found with id: 1"));
	}

	@Test
	public void testEditNewUser() throws Exception {
		mvc.perform(get("/newUser")).andExpect(view().name("edit-user"))
				.andExpect(model().attribute("user", new User())).andExpect(model().attribute("message", ""));

		verifyNoMoreInteractions(ecommerceService);
	}

	@Test
	public void testEditNewOrder() throws Exception {
		mvc.perform(get("/newOrder")).andExpect(view().name("edit-order"))
				.andExpect(model().attribute("order", new Order())).andExpect(model().attribute("message", ""));

		verifyNoMoreInteractions(ecommerceService);
	}

	@Test
	public void testPostUserWithoutIdShouldInsertNewUser() throws Exception {
		mvc.perform(post("/saveUser").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(ecommerceService).insertNewUser(new User(null, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testPostUserWithIdShouldUpdateExistingUser() throws Exception {
		mvc.perform(post("/saveUser").param("id", "1").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(ecommerceService).updateUserById(1L, new User(1L, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testPostOrderWithoutIdShouldInsertNewOrder() throws Exception {
		mvc.perform(post("/saveOrder").param("item", "BOX1").param("price", "700").param("user.name", "test")
				.param("user.surname", "test").param("user.email", "test").param("user.balance", "2000"))
				.andExpect(redirectedUrl("/orders"));

		verify(ecommerceService).insertNewOrder(new Order(null, Item.BOX1, 700, new User(1L, "test", "test", "test", 2000)));
	}

	@Test
	public void testPostOrderWhenInsertFailsShouldHandleException() throws Exception {
		doThrow(new IllegalStateException("Unable to insert new order")).when(ecommerceService)
				.insertNewOrder(any(Order.class));

		mvc.perform(post("/saveOrder").param("item", "BOX1").param("price", "700").param("user.name", "test")
				.param("user.username", "test").param("user.email", "test").param("user.balance", "500"))
				.andExpect(redirectedUrl("/orders"))
				.andExpect(flash().attribute("error", "Unable to insert new order"));

		verify(ecommerceService).insertNewOrder(any(Order.class));
		verifyNoMoreInteractions(ecommerceService);
	}

	@Test
	public void testPostOrderWithIdShouldUpdateExistingOrder() throws Exception {
		mvc.perform(post("/saveOrder").param("id", "1").param("item", "BOX1").param("price", "700")
				.param("user.name", "test").param("user.surname", "test").param("user.email", "test")
				.param("user.balance", "2000")).andExpect(redirectedUrl("/orders"));

		verify(ecommerceService).updateOrderById(1L,
				new Order(1L, Item.BOX1, 700, new User(1L, "test", "test", "test", 2000)));
	}

	@Test
	public void testPostOrderWhenUpdateFailsShouldHandleException() throws Exception {
		doThrow(new IllegalStateException("Unable to update the order")).when(ecommerceService).updateOrderById(anyLong(),
				any(Order.class));

		mvc.perform(post("/saveOrder").param("id", "1").param("item", "BOX1").param("price", "700")
				.param("user.name", "test").param("user.username", "test").param("user.email", "test")
				.param("user.balance", "500")).andExpect(redirectedUrl("/orders"))
				.andExpect(flash().attribute("error", "Unable to update the order"));

		verify(ecommerceService).updateOrderById(anyLong(), any(Order.class));
		verifyNoMoreInteractions(ecommerceService);
	}

	@Test
	public void testHandleBalanceWhenUserIsNotFound() throws Exception {
		when(ecommerceService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/1/handle_balance")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("user", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testDepositWhenSuccessShouldRedirectToMappingUsers() throws Exception {
		mvc.perform(post("/1/deposit").param("amount", "500")).andExpect(redirectedUrl("/"));

		verify(ecommerceService).deposit(1L, 500);
	}

	@Test
	public void testDepositWhenFailsShouldHandleIllegalArgumentException() throws Exception {
		doThrow(new IllegalArgumentException("Deposit amount cannot be negative")).when(ecommerceService).deposit(anyLong(),
				anyLong());

		mvc.perform(post("/1/deposit").param("amount", "-500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "Deposit amount cannot be negative"));

		verify(ecommerceService).deposit(anyLong(), anyLong());
	}

	@Test
	public void testDepositWhenFailsShouldHandleIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("User not found")).when(ecommerceService).deposit(anyLong(), anyLong());

		mvc.perform(post("/1/deposit").param("amount", "500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "User not found"));

		verify(ecommerceService).deposit(anyLong(), anyLong());
	}

	@Test
	public void testWithdrawWhenSuccessShouldRedirectToMappingUsers() throws Exception {
		mvc.perform(post("/1/withdraw").param("amount", "500")).andExpect(redirectedUrl("/"));

		verify(ecommerceService).withdraw(1L, 500);
	}

	@Test
	public void testWithdrawWhenFailsShouldHandleIllegalArgumentException() throws Exception {
		doThrow(new IllegalArgumentException("Withdraw amount cannot be negative")).when(ecommerceService)
				.withdraw(anyLong(), anyLong());

		mvc.perform(post("/1/withdraw").param("amount", "-500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "Withdraw amount cannot be negative"));

		verify(ecommerceService).withdraw(anyLong(), anyLong());
	}

	@Test
	public void testWithdrawWhenFailsShouldHandleIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("User not found")).when(ecommerceService).withdraw(anyLong(), anyLong());

		mvc.perform(post("/1/withdraw").param("amount", "500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "User not found"));

		verify(ecommerceService).withdraw(anyLong(), anyLong());
	}
}
