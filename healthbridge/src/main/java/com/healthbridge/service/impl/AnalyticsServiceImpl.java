package com.healthbridge.service.impl;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthbridge.dto.AnalyticsResponse;
import com.healthbridge.entity.SymptomRecord;
import com.healthbridge.entity.User;
import com.healthbridge.repository.SymptomRepository;
import com.healthbridge.repository.UserRepository;
import com.healthbridge.service.AnalyticsService;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SymptomRepository symptomRepo;

	@Override
	public AnalyticsResponse getAnalytics() {
		long totalUsers = userRepo.count();

		List<SymptomRecord> records = symptomRepo.findAll();

		// Most common symptoms per region (city)
		Map<Object, Long> commonSymptomsByRegion = records.stream().collect(Collectors.groupingBy(
				r -> r.getUser().getCity() != null ? r.getUser().getCity() : "Unknown", Collectors.counting()));

		Map<String, Long> severityTrends = records.stream().collect(Collectors.groupingBy(
				r -> r.getSeverityLevel() != null ? r.getSeverityLevel() : "Unknown", Collectors.counting()));
		// Languages used
		List<User> users = userRepo.findAll();
		Map<String, Long> languagesUsed = users.stream().collect(Collectors
				.groupingBy(u -> u.getLanguage() != null ? u.getLanguage() : "Unknown", Collectors.counting()));

		return AnalyticsResponse.builder().totalUsersHelped(totalUsers).commonSymptomsByRegion(commonSymptomsByRegion)
				.severityTrends(severityTrends).languagesUsed(languagesUsed).build();
	}
}