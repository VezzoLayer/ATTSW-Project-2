package com.ecommerce.manager.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.repositories.OrderRepository;

@Service
public class OrderService {

	private OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

}
