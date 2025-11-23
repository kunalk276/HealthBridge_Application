package com.healthbridge.service.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthbridge.dto.UserAnalyticsResponse;
import com.healthbridge.repository.SymptomRepository;
import com.healthbridge.service.UserAnalyticsService;

@Service
public class UserAnalyticsServiceImpl implements UserAnalyticsService {

	@Autowired
	private SymptomRepository symptomRepo;

	@Override
	public com.healthbridge.dto.UserAnalyticsResponse getUserAnalytics(Long userId) {
		List<Object[]> topSymptoms = symptomRepo.findTopSymptomsByUser(userId);
		List<Object[]> severityTrend = symptomRepo.findSeverityTrendByUser(userId);

		Map<String, Long> symptomMap = new HashMap<>();
		for (Object[] row : topSymptoms) {
			symptomMap.put((String) row[0], (Long) row[1]);
		}

		Map<String, Long> severityMap = new HashMap<>();
		for (Object[] row : severityTrend) {
			severityMap.put((String) row[0], (Long) row[1]);
		}

		long totalRecords = symptomMap.values().stream().mapToLong(Long::longValue).sum();
		String healthStatus = computeHealthStatus(severityMap);

		return UserAnalyticsResponse.builder().totalRecords(totalRecords).commonSymptoms(symptomMap)
				.severityDistribution(severityMap).healthStatus(healthStatus).build();
	}

	private String computeHealthStatus(Map<String, Long> severityMap) {
		if (severityMap == null || severityMap.isEmpty()) {
			return "No data yet";
		}

		long high = 0L;
		long emergency = 0L;

		for (Map.Entry<String, Long> entry : severityMap.entrySet()) {
			String key = entry.getKey().toLowerCase().trim();
			Long count = entry.getValue();

			// Normalize by removing non-ASCII chars for safer matching
			String normalized = key.replaceAll("[^\\p{IsAlphabetic}]", "");

			// EMERGENCY or critical conditions (for all 7 languages)
			if (normalized.contains("emergency") || normalized.contains("आपात") || normalized.contains("आपत")
					|| normalized.contains("आपातकालीन") || normalized.contains("आपत्कालीन")
					|| normalized.contains("आपत्कालीन") || normalized.contains("आपातकालिन")
					|| normalized.contains("आपातकालीन") || normalized.contains("आपातकालिन")
					|| normalized.contains("આપત્કાળીન") || normalized.contains("અત્યાવશ્યક")
					|| normalized.contains("அவசரம்") || normalized.contains("అత్యవసర")
					|| normalized.contains("അത്യാഹിതം")) {
				emergency += count;
				continue;
			}

			// HIGH severity (all supported languages)
			if (normalized.contains("high") || normalized.contains("उच्च") || normalized.contains("उच्चस्तर")
					|| normalized.contains("उच्चस्तरीय") || normalized.contains("ઉચ્ચ") || normalized.contains("உயர்")
					|| normalized.contains("అధిక") || normalized.contains("ഉയർന്ന")) {
				high += count;
			}
		}

		long total = severityMap.values().stream().mapToLong(Long::longValue).sum();
		if (total == 0)
			return "No data yet";

		// Weighted formula
		double risk = ((high * 2.0) + (emergency * 3.0)) / total;

		// log.info("🩺 Risk calculation: high={}, emergency={}, total={}, risk={}",
		// high, emergency, total, risk);

		// Determine health status
		if (emergency > 0 || risk > 2.0)
			return " Critical — Immediate medical attention needed";
		if (high > 0 || risk > 1.0)
			return " Moderate — Monitor symptoms closely";
		return " Stable — No serious issue detected";
	}
}