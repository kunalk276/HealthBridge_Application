package com.healthbridge.dto;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SymptomTrends {
	private Map<String, Long> commonSymptoms;
	private Map<String, Long> severityDistribution;
}