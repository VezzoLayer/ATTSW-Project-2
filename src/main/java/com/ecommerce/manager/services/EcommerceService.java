package com.ecommerce.manager.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.OrderRepository;
import com.ecommerce.manager.repositories.UserRepository;

@Service
public class EcommerceService {

	private UserRepository userRepository;
	private OrderRepository orderRepository;

	public EcommerceService(UserRepository userRepository, OrderRepository orderRepository) {
		this.userRepository = userRepository;
		this.orderRepository = orderRepository;
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	public User getUserById(long id) {
		return userRepository.findById(id).orElse(null);
	}

	public Order getOrderById(long id) {
		return orderRepository.findById(id).orElse(null);
	}

	public User insertNewUser(User user) {
		user.setId(null);
		return userRepository.save(user);
	}

	@Transactional
	public Order insertNewOrder(Order order) {
		order.setId(null);

		try {
			withdraw(order.getUser().getId(), order.getPrice());
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw new IllegalStateException("Unable to insert new order");
		}

		return orderRepository.save(order);
	}

	public User updateUserById(long id, User replacement) {
		replacement.setId(id);
		return userRepository.save(replacement);
	}

	@Transactional
	public Order updateOrderById(long id, Order replacement) {
		Order existing = orderRepository.findById(id).orElse(null);

		replacement.setId(id);

		try {
			deposit(existing.getUser().getId(), existing.getPrice());
			withdraw(replacement.getUser().getId(), replacement.getPrice());
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw new IllegalStateException("Unable to update the order");
		}

		return orderRepository.save(replacement);
	}

	public void deposit(long id, long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Deposit amount cannot be negative");
		}

		User user = userRepository.findById(id).orElse(null);

		if (user == null) {
			throw new IllegalStateException("User not found");
		}

		user.setBalance(user.getBalance() + amount);
		userRepository.save(user);
	}

	public void withdraw(long id, long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Withdraw amount cannot be negative");
		}

		User user = userRepository.findById(id).orElse(null);

		if (user == null) {
			throw new IllegalStateException("User not found");
		}

		if (user.getBalance() - amount < 0) {
			throw new IllegalStateException("Not enough balance to perform withdraw");
		}

		user.setBalance(user.getBalance() - amount);
		userRepository.save(user);
	}

}
