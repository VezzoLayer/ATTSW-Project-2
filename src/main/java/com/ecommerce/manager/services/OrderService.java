package com.ecommerce.manager.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public Order insertNewOrder(Order order) {
		order.setId(null);

		try {
			userService.withdraw(order.getUser().getId(), order.getPrice());
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw new IllegalStateException("Unable to insert new order");
		}

		return orderRepository.save(order);
	}

	@Transactional
	public Order updateOrderById(long id, Order replacement) {
		Order existing = orderRepository.findById(id).orElse(null);

		replacement.setId(id);

		try {
			userService.deposit(existing.getUser().getId(), existing.getPrice());
			userService.withdraw(replacement.getUser().getId(), replacement.getPrice());
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw new IllegalStateException("Unable to update the order");
		}

		return orderRepository.save(replacement);
	}

}
