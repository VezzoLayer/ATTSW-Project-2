package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
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

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

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
	private UserService userService;

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

		when(userService.getAllUsers()).thenReturn(users);

		mvc.perform(get("/")).andExpect(view().name("index")).andExpect(model().attribute("users", users))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testHomeViewShowsMessageWhenThereAreNoUsers() throws Exception {
		when(userService.getAllUsers()).thenReturn(Collections.emptyList());

		mvc.perform(get("/")).andExpect(view().name("index"))
				.andExpect(model().attribute("users", Collections.emptyList()))
				.andExpect(model().attribute("message", "No user to show"));
	}

	@Test
	@Parameters({ "/editUser/1, edit-user", "/user/1/orders, all-orders", "/1/handle_balance, handle-balance" })
	public void testParameterizedMappingNameWhenTheUserIsFound(String mappingName, String expectedView)
			throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(get(mappingName)).andExpect(view().name(expectedView)).andExpect(model().attribute("user", user))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testEditUserWhenUserIsNotFound() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/editUser/1")).andExpect(view().name("edit-user"))
				.andExpect(model().attribute("user", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testEditNewUser() throws Exception {
		mvc.perform(get("/newUser")).andExpect(view().name("edit-user"))
				.andExpect(model().attribute("user", new User())).andExpect(model().attribute("message", ""));

		verifyNoMoreInteractions(userService);
	}

	@Test
	public void testPostUserWithoutIdShouldInsertNewUser() throws Exception {
		mvc.perform(post("/saveUser").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(userService).insertNewUser(new User(null, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testPostUserWithIdShouldUpdateExistingUser() throws Exception {
		mvc.perform(post("/saveUser").param("id", "1").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(userService).updateUserById(1L, new User(1L, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testAllOrdersWhenUserIsNotFound() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/user/1/orders")).andExpect(view().name("all-orders"))
				.andExpect(model().attribute("user", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testHandleBalanceWhenUserIsNotFound() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/1/handle_balance")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("User", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testDepositWhenSuccessShouldRedirectToMappingUsers() throws Exception {
		mvc.perform(post("/1/deposit").param("amount", "500")).andExpect(redirectedUrl("/"));

		verify(userService).deposit(1L, 500);
	}

	@Test
	public void testDepositWhenFailsShouldHandleIllegalArgumentException() throws Exception {
		doThrow(new IllegalArgumentException("Deposit amount cannot be negative")).when(userService).deposit(anyLong(),
				anyLong());

		mvc.perform(post("/1/deposit").param("amount", "-500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "Deposit amount cannot be negative"));

		verify(userService).deposit(anyLong(), anyLong());
	}

	@Test
	public void testDepositWhenFailsShouldHandleIllegalStateException() throws Exception {
		doThrow(new IllegalStateException("User not found")).when(userService).deposit(anyLong(), anyLong());

		mvc.perform(post("/1/deposit").param("amount", "-500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "User not found"));

		verify(userService).deposit(anyLong(), anyLong());
	}

	@Test
	public void testWithdrawWhenSuccessShouldRedirectToMappingUsers() throws Exception {
		mvc.perform(post("/1/withdraw").param("amount", "500")).andExpect(redirectedUrl("/"));

		verify(userService).withdraw(1L, 500);
	}

	@Test
	public void testWithdrawWhenFailsShouldHandleIllegalArgumentException() throws Exception {
		doThrow(new IllegalArgumentException("Withdraw amount cannot be negative")).when(userService)
				.withdraw(anyLong(), anyLong());

		mvc.perform(post("/1/withdraw").param("amount", "-500")).andExpect(redirectedUrl("/"))
				.andExpect(flash().attribute("error", "Withdraw amount cannot be negative"));

		verify(userService).withdraw(anyLong(), anyLong());
	}
}
