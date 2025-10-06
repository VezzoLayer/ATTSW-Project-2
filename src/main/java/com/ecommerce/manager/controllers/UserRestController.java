package com.ecommerce.manager.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

	private UserService userService;

	private static final String MESSAGE = "message";

	public UserRestController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<User> allUsers() {
		return userService.getAllUsers();
	}

	@GetMapping("/{id}")
	public User oneUser(@PathVariable long id) {
		return userService.getUserById(id);
	}

	@PostMapping("/new")
	public User newUser(@RequestBody User user) {
		return userService.insertNewUser(user);
	}

	@PutMapping("/update/{id}")
	public User updateUser(@PathVariable long id, @RequestBody User user) {
		return userService.updateUserById(id, user);
	}

	@PostMapping("/{id}/deposit")
	public ResponseEntity<Void> deposit(@PathVariable long id, @RequestBody long amount) {
		userService.deposit(id, amount);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/withdraw")
	public ResponseEntity<Void> withdraw(@PathVariable long id, @RequestBody long amount) {
		userService.withdraw(id, amount);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE, ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
		if (ex.getMessage().contains("Not enough balance to perform withdraw")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE, ex.getMessage()));
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE, ex.getMessage()));
	}
}
