package com.healthbridge.dto;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsResponse {

	private long totalUsersHelped;
	//  Most common symptoms per region (e.g., city)
	private Map<Object, Long> commonSymptomsByRegion;

	//  Severity trends (LOW, MEDIUM, HIGH, EMERGENCY)
	private Map<String, Long> severityTrends;

	//  Languages used (en, hi, mr, etc.)
	private Map<String, Long> languagesUsed;
}