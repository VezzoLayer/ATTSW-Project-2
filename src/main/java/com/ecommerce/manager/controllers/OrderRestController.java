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
import com.ecommerce.manager.services.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

	private OrderService orderService;

	private static final String MESSAGE_ATTRIBUTE = "message";

	public OrderRestController(OrderService orderService) {
		this.orderService = orderService;
	}

	/*
	 * @GetMapping public List<Order> allOrders() { return
	 * orderService.getAllOrders(); }
	 * 
	 * @GetMapping("/{id}") public Order oneOrder(@PathVariable long id) { return
	 * orderService.getOrderById(id); }
	 */
	
	@PostMapping("/new")
	public Order newOrder(@RequestBody Order order) {
		return orderService.insertNewOrder(order);
	}

	@PutMapping("/update/{id}")
	public Order updateOrder(@PathVariable long id, @RequestBody Order order) {
		return orderService.updateOrderById(id, order);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(MESSAGE_ATTRIBUTE, ex.getMessage()));
	}
}
