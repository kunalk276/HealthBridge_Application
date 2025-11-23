package com.healthbridge.dto;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiAnalytics {
	private long totalAiResponses;
	private Map<String, Long> languageWisePerformance;
	private Map<String, Long> referralStats;
}