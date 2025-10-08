package com.ecommerce.manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ecommerce.manager.services.UserService;

@Controller
public class UserWebController {

	private static final String USERS_ATTRIBUTE = "users";

	private UserService userService;

	public UserWebController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute(USERS_ATTRIBUTE, userService.getAllUsers());

		return "index";
	}
}
