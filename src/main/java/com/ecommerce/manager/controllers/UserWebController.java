package com.ecommerce.manager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserWebController {

	@GetMapping("/")
	public String index() {
		return "index";
	}
}
