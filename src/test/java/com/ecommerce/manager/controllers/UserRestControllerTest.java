package com.ecommerce.manager.controllers;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
	public void testOneUserByIdWithExistingUser() throws Exception {
		when(userService.getUserById(anyLong())).thenReturn(new User(1L, "user 1", "test", "test", 3000));
		this.mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.username", is("user 1")))
				.andExpect(jsonPath("$.name", is("test"))).andExpect(jsonPath("$.email", is("test")))
				.andExpect(jsonPath("$.balance", is(3000)));
	}
}
