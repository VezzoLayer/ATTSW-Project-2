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

	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String ORDER_ATTRIBUTE = "order";
	private static final String ORDERS_ATTRIBUTE = "orders";

	private OrderService orderService;

	public OrderWebController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/orders")
	public String orders(Model model) {
		List<Order> allOrders = orderService.getAllOrders();

		model.addAttribute(ORDERS_ATTRIBUTE, orderService.getAllOrders());
		model.addAttribute(MESSAGE_ATTRIBUTE, allOrders.isEmpty() ? "No order to show" : "");

		return "orders";
	}

	@GetMapping("/editOrder/{id}")
	public String editOrder(@PathVariable long id, Model model) {
		Order orderById = orderService.getOrderById(id);

		model.addAttribute(ORDER_ATTRIBUTE, orderById);
		model.addAttribute(MESSAGE_ATTRIBUTE, orderById == null ? "No order found with id: " + id : "");

		return "edit-order";
	}

	@GetMapping("/newOrder")
	public String newOrder(Model model) {
		model.addAttribute(ORDER_ATTRIBUTE, new Order());
		model.addAttribute(MESSAGE_ATTRIBUTE, "");

		return "edit-order";
	}

	@PostMapping("/saveOrder")
	public String saveOrder(Order order) {
		final Long id = order.getId();

		if (id == null) {
			orderService.insertNewOrder(order);
		} else {
			orderService.updateOrderById(id, order);
		}

		return "orders";
	}
}
