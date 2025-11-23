package com.healthbridge.service;


import com.healthbridge.dto.UserAnalyticsResponse;

public interface UserAnalyticsService {

	/**
	 * Returns detailed analytics for a specific user. Includes their most common
	 * symptoms, severity distribution, total number of records, and current health
	 * status summary.
	 *
	 * @param userId the ID of the user whose analytics are requested
	 * @return a UserAnalyticsResponse containing the analytics data
	 */
	UserAnalyticsResponse getUserAnalytics(Long userId);
}