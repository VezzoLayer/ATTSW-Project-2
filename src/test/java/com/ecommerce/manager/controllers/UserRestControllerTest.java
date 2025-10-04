package com.ecommerce.manager.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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
}
