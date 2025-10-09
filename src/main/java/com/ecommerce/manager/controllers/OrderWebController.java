package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

}
