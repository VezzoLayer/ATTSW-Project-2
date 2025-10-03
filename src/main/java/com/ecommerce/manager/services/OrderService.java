package com.ecommerce.manager.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.repositories.OrderRepository;

@Service
public class OrderService {

	private OrderRepository orderRepository;
	private UserService userService;

	public OrderService(OrderRepository orderRepository, UserService userService) {
		this.orderRepository = orderRepository;
		this.userService = userService;
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	public Order getOrderById(long id) {
		return orderRepository.findById(id).orElse(null);
	}

	public Order insertNewOrder(Order order) {
		order.setId(null);

		userService.withdraw(order.getUser().getId(), order.getPrice());

		return orderRepository.save(order);
	}

	public Order updateOrderById(long id, Order replacement) {
		replacement.setId(id);
		return orderRepository.save(replacement);
	}

}
