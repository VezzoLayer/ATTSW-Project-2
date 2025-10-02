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
		User user = userRepository.findById(id).orElse(null);
		user.setBalance(user.getBalance() + amount);

		return userRepository.save(user);
	}
}
