package com.healthbridge.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthbridge.dto.AdminAnalyticsResponse;
import com.healthbridge.dto.AiAnalytics;
import com.healthbridge.dto.RegionalHealth;
import com.healthbridge.dto.SymptomTrends;
import com.healthbridge.dto.UserOverview;
import com.healthbridge.repository.SymptomRecordRepository;
import com.healthbridge.repository.UserRepository;
import com.healthbridge.service.AdminAnalyticsService;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SymptomRecordRepository symptomRecordRepository;

	@Override
	public AdminAnalyticsResponse getAnalytics() {

		// USER OVERVIEW

		long totalUsers = userRepository.count();
		Map<String, Long> usersByState = safeConvert(userRepository.countUsersByState());
		Map<String, Long> usersByCity = safeConvert(userRepository.countUsersByCity());
		Map<String, Long> usersByLanguage = safeConvert(userRepository.countUsersByLanguage());
		List<String> recentUsers = userRepository.findRecentUsers();

		UserOverview userOverview = new UserOverview(totalUsers, usersByState, usersByCity, usersByLanguage,
				recentUsers);

		// SYMPTOM TRENDS

		Map<String, Long> commonSymptoms = safeConvert(symptomRecordRepository.countCommonSymptoms());
		Map<String, Long> severityDist = safeConvert(symptomRecordRepository.countBySeverity());

		SymptomTrends symptomTrends = new SymptomTrends(commonSymptoms, severityDist);

		// REGIONAL HEALTH (Nested Map)

		Map<String, Long> severitySummary = safeConvert(symptomRecordRepository.countSeveritySummary());

		// Build nested manually: { STATE : { SEVERITY : COUNT } }
		Map<String, Map<String, Long>> severityMap = new HashMap<>();

		for (Map.Entry<String, Long> entry : severitySummary.entrySet()) {
			String severity = entry.getKey();
			Long count = entry.getValue();

			// store as "ALL_STATES" → severity summary
			severityMap.computeIfAbsent("ALL_STATES", k -> new HashMap<>()).put(severity, count);
		}

		List<String> hotspots = severityMap.entrySet().stream().filter(e -> e.getValue().getOrDefault("HIGH", 0L) > 10)
				.map(Map.Entry::getKey).toList();

		RegionalHealth regional = new RegionalHealth(severityMap, hotspots);

		// AI ANALYTICS

		long totalAiResponses = symptomRecordRepository.count();
		Map<String, Long> langPerf = safeConvert(symptomRecordRepository.countLanguageWiseAi());
		Map<String, Long> referralStats = safeConvert(symptomRecordRepository.countReferralStats());

		AiAnalytics ai = new AiAnalytics(totalAiResponses, langPerf, referralStats);

		return new AdminAnalyticsResponse(userOverview, symptomTrends, regional, ai);
	}

	// SAFE CONVERSION (Handles null, missing values, index errors)

	private Map<String, Long> safeConvert(List<Object[]> list) {
		Map<String, Long> map = new HashMap<>();

		if (list == null)
			return map;

		for (Object[] row : list) {
			if (row == null || row.length < 2)
				continue;

			String key = (row[0] == null) ? "UNKNOWN" : row[0].toString();
			Long value = (row[1] == null) ? 0L : Long.parseLong(row[1].toString());

			map.put(key, value);
		}

		return map;
	}
}