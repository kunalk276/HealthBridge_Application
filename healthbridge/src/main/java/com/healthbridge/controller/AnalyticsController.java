package com.healthbridge.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthbridge.dto.ApiResult;
import com.healthbridge.dto.UserAnalyticsResponse;
import com.healthbridge.service.UserAnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {

	@Autowired
	private UserAnalyticsService userAnalyticsService;

	// Get user-level analytics (symptoms, trends, health score)
	@GetMapping("/user/{id}")
	public ResponseEntity<ApiResult<?>> getUserAnalytics(@PathVariable Long id) {
		UserAnalyticsResponse analytics = userAnalyticsService.getUserAnalytics(id);
		return ResponseEntity.ok(ApiResult.ok(analytics));
	}
}
