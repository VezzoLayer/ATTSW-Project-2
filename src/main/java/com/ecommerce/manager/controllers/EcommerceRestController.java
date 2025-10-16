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

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.EcommerceService;

@RestController
@RequestMapping("/api")
public class EcommerceRestController {

	private EcommerceService ecommerceService;

	private static final String MESSAGE_ATTRIBUTE = "message";

	public EcommerceRestController(EcommerceService ecommerceService) {
		this.ecommerceService = ecommerceService;
	}

	@GetMapping("/users")
	public List<User> allUsers() {
		return ecommerceService.getAllUsers();
	}

	@GetMapping("/orders")
	public List<Order> allOrders() {
		return ecommerceService.getAllOrders();
	}

	@GetMapping("/users/{id}")
	public User oneUser(@PathVariable long id) {
		return ecommerceService.getUserById(id);
	}

	@GetMapping("/orders/{id}")
	public Order oneOrder(@PathVariable long id) {
		return ecommerceService.getOrderById(id);
	}

	@PostMapping("/users/new")
	public User newUser(@RequestBody User user) {
		return ecommerceService.insertNewUser(user);
	}

	@PostMapping("/orders/new")
	public Order newOrder(@RequestBody Order order) {
		return ecommerceService.insertNewOrder(order);
	}

	@PutMapping("/users/update/{id}")
	public User updateUser(@PathVariable long id, @RequestBody User user) {
		return ecommerceService.updateUserById(id, user);
	}

	@PutMapping("/orders/update/{id}")
	public Order updateOrder(@PathVariable long id, @RequestBody Order order) {
		return ecommerceService.updateOrderById(id, order);
	}

	@PostMapping("/users/{id}/deposit")
	public ResponseEntity<Void> deposit(@PathVariable long id, @RequestBody long amount) {
		ecommerceService.deposit(id, amount);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/users/{id}/withdraw")
	public ResponseEntity<Void> withdraw(@PathVariable long id, @RequestBody long amount) {
		ecommerceService.withdraw(id, amount);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_ATTRIBUTE, ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
		if (ex.getMessage().contains("Not enough balance to perform withdraw")
				| ex.getMessage().contains("Unable to")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_ATTRIBUTE, ex.getMessage()));
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE_ATTRIBUTE, ex.getMessage()));
	}
}
