package com.healthbridge.service;


import java.util.Optional;

import com.healthbridge.entity.User;

public interface UserService {
	User register(User user);

	Optional<User> findByUsername(String username);
}
