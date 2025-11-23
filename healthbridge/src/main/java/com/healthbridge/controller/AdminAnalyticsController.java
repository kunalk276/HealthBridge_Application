package com.healthbridge.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthbridge.dto.ApiResult;
import com.healthbridge.service.AdminAnalyticsService;

@RestController
@RequestMapping("/api/admin/analytics")

public class AdminAnalyticsController {

	@Autowired
	private AdminAnalyticsService analyticsService; // Injects service layer dependency to fetch analytics data

	/**
	 * GET API to fetch overall admin analytics.
	 * 
	 */
	@GetMapping // Handles HTTP GET request for /api/admin/analytics
	public ResponseEntity<ApiResult<?>> getAnalytics() {

		// Fetch analytics from service and return as a successful response
		return ResponseEntity.ok(ApiResult.ok(analyticsService.getAnalytics()));
	}
}