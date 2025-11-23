package com.healthbridge.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAnalyticsResponse {
	private UserOverview userOverview;
	private SymptomTrends symptomTrends;
	private RegionalHealth regionalHealth;
	private AiAnalytics aiAnalytics;
}