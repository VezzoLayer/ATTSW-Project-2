package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.services.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

	private OrderService orderService;

	public OrderRestController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<Order> allOrders() {
		return orderService.getAllOrders();
	}

	@GetMapping("/{id}")
	public Order oneOrder(@PathVariable long id) {
		return orderService.getOrderById(id);
	}
}
