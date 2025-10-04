package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

	private UserService userService;

	public UserRestController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<User> allUsers() {
		return userService.getAllUsers();
	}
}
