package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.UserService;

@Controller
public class UserWebController {

	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String ERROR_ATTRIBUTE = "error";
	private static final String USERS_ATTRIBUTE = "users";
	private static final String USER_ATTRIBUTE = "user";

	private UserService userService;

	public UserWebController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public String index(Model model) {
		List<User> allUsers = userService.getAllUsers();

		model.addAttribute(USERS_ATTRIBUTE, userService.getAllUsers());
		model.addAttribute(MESSAGE_ATTRIBUTE, allUsers.isEmpty() ? "No user to show" : "");

		return "index";
	}

	@GetMapping("/edit/{id}")
	public String editUser(@PathVariable long id, Model model) {
		User UserById = userService.getUserById(id);

		model.addAttribute(USER_ATTRIBUTE, UserById);
		model.addAttribute(MESSAGE_ATTRIBUTE, UserById == null ? "No user found with id: " + id : "");

		return "edit-user";
	}

	@GetMapping("/new")
	public String newUser(Model model) {
		model.addAttribute(USER_ATTRIBUTE, new User());
		model.addAttribute(MESSAGE_ATTRIBUTE, "");

		return "edit-user";
	}

	@PostMapping("/save")
	public String saveUser(User user) {
		final Long id = user.getId();

		if (id == null) {
			userService.insertNewUser(user);
		} else {
			userService.updateUserById(id, user);
		}

		return "redirect:/";
	}

	@GetMapping("/{id}/handle_balance")
	public String handleBalance(@PathVariable long id, Model model) {
		User UserById = userService.getUserById(id);

		model.addAttribute(USER_ATTRIBUTE, UserById);
		model.addAttribute(MESSAGE_ATTRIBUTE, UserById == null ? "No user found with id: " + id : "");

		return "handle-balance";
	}

	@PostMapping("/{id}/deposit")
	public String deposit(@PathVariable long id, @RequestParam long amount, Model model) {
		if (amount < 0) {
			model.addAttribute(ERROR_ATTRIBUTE, "Importo negativo non ammesso");
			model.addAttribute(USER_ATTRIBUTE, userService.getUserById(id));
			return "handle-balance";
		}

		userService.deposit(id, amount);
		return "redirect:/";
	}

	@PostMapping("/{id}/withdraw")
	public String withdraw(@PathVariable long id, @RequestParam long amount, Model model) {
		if (amount < 0) {
			model.addAttribute(ERROR_ATTRIBUTE, "Importo negativo non ammesso");
			model.addAttribute(USER_ATTRIBUTE, userService.getUserById(id));
			return "handle-balance";
		}

		userService.withdraw(id, amount);
		return "redirect:/";
	}
}
