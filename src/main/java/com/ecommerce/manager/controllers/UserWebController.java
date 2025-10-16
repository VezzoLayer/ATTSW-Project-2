package com.ecommerce.manager.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.manager.model.Order;
import com.ecommerce.manager.model.User;
import com.ecommerce.manager.services.EcommerceService;

@Controller
public class UserWebController {

	private static final String MESSAGE_ATTRIBUTE = "message";
	private static final String ERROR_ATTRIBUTE = "error";

	private static final String USER_ATTRIBUTE = "user";
	private static final String ORDER_ATTRIBUTE = "order";

	private static final String USERS_ATTRIBUTE = "users";
	private static final String ORDERS_ATTRIBUTE = "orders";

	private static final String REDIRECT_TO_MAPPING_USERS = "redirect:/";
	private static final String REDIRECT_TO_MAPPING_ORDERS = "redirect:/orders";

	private EcommerceService ecommerceService;

	public UserWebController(EcommerceService ecommerceService) {
		this.ecommerceService = ecommerceService;
	}

	@GetMapping("/")
	public String index(Model model) {
		List<User> allUsers = ecommerceService.getAllUsers();

		model.addAttribute(USERS_ATTRIBUTE, allUsers);
		model.addAttribute(MESSAGE_ATTRIBUTE, allUsers.isEmpty() ? "No user to show" : "");

		return "index";
	}

	@GetMapping("/orders")
	public String orders(Model model) {
		List<Order> allOrders = ecommerceService.getAllOrders();

		model.addAttribute(ORDERS_ATTRIBUTE, allOrders);
		model.addAttribute(MESSAGE_ATTRIBUTE, allOrders.isEmpty() ? "No order to show" : "");

		return "all-orders";
	}

	@GetMapping("/editUser/{id}")
	public String editUser(@PathVariable long id, Model model) {
		User userById = ecommerceService.getUserById(id);

		model.addAttribute(USER_ATTRIBUTE, userById);
		model.addAttribute(MESSAGE_ATTRIBUTE, userById == null ? "No user found with id: " + id : "");

		return "edit-user";
	}

	@GetMapping("/editOrder/{id}")
	public String editOrder(@PathVariable long id, Model model) {
		Order orderById = ecommerceService.getOrderById(id);

		model.addAttribute(ORDER_ATTRIBUTE, orderById);
		model.addAttribute(MESSAGE_ATTRIBUTE, orderById == null ? "No order found with id: " + id : "");

		return "edit-order";
	}

	@GetMapping("/newUser")
	public String newUser(Model model) {
		model.addAttribute(USER_ATTRIBUTE, new User());
		model.addAttribute(MESSAGE_ATTRIBUTE, "");

		return "edit-user";
	}

	@GetMapping("/newOrder")
	public String newOrder(Model model) {
		model.addAttribute(ORDER_ATTRIBUTE, new Order());
		model.addAttribute(MESSAGE_ATTRIBUTE, "");

		return "edit-order";
	}

	@PostMapping("/saveUser")
	public String saveUser(User user) {
		final Long id = user.getId();

		if (id == null) {
			ecommerceService.insertNewUser(user);
		} else {
			ecommerceService.updateUserById(id, user);
		}

		return REDIRECT_TO_MAPPING_USERS;
	}

	@PostMapping("/saveOrder")
	public String saveOrder(Order order) {
		final Long id = order.getId();

		if (id == null) {
			ecommerceService.insertNewOrder(order);
		} else {
			ecommerceService.updateOrderById(id, order);
		}

		return REDIRECT_TO_MAPPING_ORDERS;
	}

	@GetMapping("/{id}/handle_balance")
	public String handleBalance(@PathVariable long id, Model model) {
		User userById = ecommerceService.getUserById(id);

		model.addAttribute(USER_ATTRIBUTE, userById);
		model.addAttribute(MESSAGE_ATTRIBUTE, userById == null ? "No user found with id: " + id : "");

		return "handle-balance";
	}

	@PostMapping("/{id}/deposit")
	public String deposit(@PathVariable long id, @RequestParam long amount) {
		ecommerceService.deposit(id, amount);

		return REDIRECT_TO_MAPPING_USERS;
	}

	@PostMapping("/{id}/withdraw")
	public String withdraw(@PathVariable long id, @RequestParam long amount) {
		ecommerceService.withdraw(id, amount);

		return REDIRECT_TO_MAPPING_USERS;
	}

	@ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
	public String handleIllegalState(RuntimeException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(ERROR_ATTRIBUTE, ex.getMessage());

		if (ex.getMessage().contains("Unable to")) {
			return REDIRECT_TO_MAPPING_ORDERS;
		}

		return REDIRECT_TO_MAPPING_USERS;
	}
}
