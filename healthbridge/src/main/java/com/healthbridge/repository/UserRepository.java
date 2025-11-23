package com.healthbridge.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.healthbridge.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	@Query("SELECT u.state, COUNT(u) FROM User u GROUP BY u.state")
	List<Object[]> countUsersByState();

	@Query("SELECT u.city, COUNT(u) FROM User u GROUP BY u.city")
	List<Object[]> countUsersByCity();

	@Query("SELECT u.language, COUNT(u) FROM User u GROUP BY u.language")
	List<Object[]> countUsersByLanguage();

	@Query("SELECT u.username FROM User u ORDER BY u.id DESC LIMIT 5")
	List<String> findRecentUsers();
}