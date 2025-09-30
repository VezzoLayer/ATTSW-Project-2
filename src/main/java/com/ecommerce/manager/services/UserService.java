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
}
