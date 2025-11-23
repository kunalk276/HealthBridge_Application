package com.healthbridge.service.impl;


import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.healthbridge.entity.User;
import com.healthbridge.repository.UserRepository;
import com.healthbridge.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	// Inject UserRepository to interact with the database
	@Autowired
	private UserRepository userRepo;

	// Inject PasswordEncoder to securely encode user passwords before saving
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public User register(User user) {

		// Check if the username already exists to avoid duplicate accounts
		if (userRepo.findByUsername(user.getUsername()).isPresent()) {
			throw new IllegalArgumentException("Username already exists. Please choose another.");
		}

		// Encode the user password before saving for security
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Set account creation timestamp
		user.setCreatedAt(LocalDateTime.now());

		// Save the new user to the database and return the saved object
		return userRepo.save(user);
	}

	@Override
	public Optional<User> findByUsername(String username) {

		// Retrieve user based on username, wrapped inside Optional to avoid null
		return userRepo.findByUsername(username);
	}
}