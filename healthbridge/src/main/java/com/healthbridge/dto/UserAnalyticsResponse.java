package com.healthbridge.dto;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponse {
	private long totalRecords;
	private Map<String, Long> commonSymptoms;
	private Map<String, Long> severityDistribution;
	private String healthStatus;

}
