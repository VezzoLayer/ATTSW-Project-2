package com.ecommerce.manager.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.repositories.UserRepository;

@Service
public class UserService {

	private UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(long id) {
		return userRepository.findById(id).orElse(null);
	}

	public User insertNewUser(User user) {
		user.setId(null);
		return userRepository.save(user);
	}

	public User updateUserById(long id, User replacement) {
		replacement.setId(id);
		return userRepository.save(replacement);
	}

	public User deposit(long id, long amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Deposit amount cannot be negative");
		}

		User user = userRepository.findById(id).orElse(null);

		if (user != null) {
			user.setBalance(user.getBalance() + amount);
			userRepository.save(user);
		}

		return user;
	}
}
