package com.healthbridge.controller;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthbridge.dto.ApiResult;
import com.healthbridge.dto.RegisterRequest;
import com.healthbridge.entity.User;
import com.healthbridge.service.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")

	public ResponseEntity<ApiResult<?>> register(@Valid @RequestBody RegisterRequest req) {
		// String r = "ROLE_ADMIN";
		User u = User.builder().username(req.getUsername()).password(req.getPassword())
				// .role(r)
				.role(req.getRole()).language(req.getLanguage()).phone(req.getPhone()).area(req.getArea())
				.city(req.getCity()).state(req.getState()).build();
		// return ResponseEntity.ok(ApiResult.ok(req));

		User saved = userService.register(u); // return
		return ResponseEntity.ok(ApiResult.ok(saved));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResult<?>> me(java.security.Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).body(ApiResult.fail("Not authenticated"));
		}

		Optional<User> userOpt = userService.findByUsername(principal.getName());
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
		}

		User user = userOpt.get();
		user.setPassword(null); // Hide password for security
		return ResponseEntity.ok(ApiResult.ok(user));
	}
}