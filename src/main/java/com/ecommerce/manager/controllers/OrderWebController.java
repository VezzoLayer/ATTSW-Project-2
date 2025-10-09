package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.services.OrderService;

@Controller
public class OrderWebController {

	private OrderService orderService;

	public OrderWebController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public String orders(Model model) {
		List<Order> allOrders = orderService.getAllOrders();

		model.addAttribute("orders", orderService.getAllOrders());
		model.addAttribute("message", allOrders.isEmpty() ? "No order to show" : "");

		return "orders";
	}

	@GetMapping("/editOrder/{id}")
	public String editOrder(@PathVariable long id, Model model) {
		Order OrderById = orderService.getOrderById(id);

		model.addAttribute("order", OrderById);
		model.addAttribute("message", OrderById == null ? "No order found with id: " + id : "");

		return "edit-order";
	}

	@GetMapping("/newOrder")
	public String newOrder(Model model) {
		model.addAttribute("order", new Order());
		model.addAttribute("message", "");

		return "edit-order";
	}

	@PostMapping("/saveOrder")
	public String saveOrder(Order order) {
		orderService.insertNewOrder(order);

		return "orders";
	}
}
