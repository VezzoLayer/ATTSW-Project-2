package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserWebController.class)
public class UserWebControllerTest {

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
	public void testEditUserWhenTheUserIsFound() throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(get("/edit/1")).andExpect(view().name("edit-user")).andExpect(model().attribute("user", user))
				.andExpect(model().attribute("message", ""));
	}

	@Test
	public void testEditUserWhenUserIsNotFound() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/edit/1")).andExpect(view().name("edit-user"))
				.andExpect(model().attribute("User", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testEditNewUser() throws Exception {
		mvc.perform(get("/new")).andExpect(view().name("edit-user")).andExpect(model().attribute("user", new User()))
				.andExpect(model().attribute("message", ""));

		verifyNoMoreInteractions(userService);
	}

	@Test
	public void testPostUserWithoutIdShouldInsertNewUser() throws Exception {
		mvc.perform(post("/save").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(userService).insertNewUser(new User(null, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testPostUserWithIdShouldUpdateExistingUser() throws Exception {
		mvc.perform(post("/save").param("id", "1").param("username", "test username").param("name", "test name")
				.param("email", "test email").param("balance", "2000")).andExpect(view().name("redirect:/"));

		verify(userService).updateUserById(1L, new User(1L, "test username", "test name", "test email", 2000));
	}

	@Test
	public void testHandleBalanceWhenTheUserIsFound() throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(get("/1/handle_balance")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("user", user)).andExpect(model().attribute("message", ""));
	}

	@Test
	public void testHandleBalanceWhenUserIsNotFound() throws Exception {
		when(userService.getUserById(1L)).thenReturn(null);

		mvc.perform(get("/1/handle_balance")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("User", nullValue()))
				.andExpect(model().attribute("message", "No user found with id: 1"));
	}

	@Test
	public void testDepositWhenAmountIsCorrect() throws Exception {
		mvc.perform(post("/1/deposit").param("amount", "500")).andExpect(view().name("redirect:/"));

		verify(userService).deposit(1L, 500);
	}

	@Test
	public void testDepositWhenAmountIsZero() throws Exception {
		mvc.perform(post("/1/deposit").param("amount", "0")).andExpect(view().name("redirect:/"));

		verify(userService).deposit(1L, 0);
	}

	@Test
	public void testDepositWhenAmountIsNotCorrect() throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(post("/1/deposit").param("amount", "-500")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("error", "Importo negativo non ammesso"))
				.andExpect(model().attribute("user", user));

		verify(userService).getUserById(1L);
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void testWithdrawWhenAmountIsCorrect() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "test", "test", "test", 1000));

		mvc.perform(post("/1/withdraw").param("amount", "500")).andExpect(view().name("redirect:/"));

		verify(userService).withdraw(1L, 500);
	}

	@Test
	public void testWithdrawWhenAmountIsZero() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "test", "test", "test", 1000));

		mvc.perform(post("/1/withdraw").param("amount", "0")).andExpect(view().name("redirect:/"));

		verify(userService).withdraw(1L, 0);
	}

	@Test
	public void testWithdrawWhenAmountIsExactlyTheBalance() throws Exception {
		when(userService.getUserById(1L)).thenReturn(new User(1L, "test", "test", "test", 1000));

		mvc.perform(post("/1/withdraw").param("amount", "1000")).andExpect(view().name("redirect:/"));

		verify(userService).withdraw(1L, 1000);
	}

	@Test
	public void testWithdrawWhenAmountIsIsNotCorrect() throws Exception {
		User user = new User(1L, "test", "test", "test", 1000);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(post("/1/withdraw").param("amount", "-500")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("error", "Importo negativo non ammesso"))
				.andExpect(model().attribute("user", user));

		verify(userService).getUserById(1L);
		verifyNoMoreInteractions(userService);
	}

	@Test
	public void testWithdrawWhenBalanceIsNotEnough() throws Exception {
		User user = new User(1L, "test", "test", "test", 300);

		when(userService.getUserById(1L)).thenReturn(user);

		mvc.perform(post("/1/withdraw").param("amount", "500")).andExpect(view().name("handle-balance"))
				.andExpect(model().attribute("error", "Saldo insufficiente"))
				.andExpect(model().attribute("user", user));

		verify(userService).getUserById(1L);
		verifyNoMoreInteractions(userService);
	}
}
