package com.healthbridge.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.healthbridge.entity.SymptomRecord;
import com.healthbridge.repository.SymptomRepository;
import com.healthbridge.repository.UserRepository;
import com.healthbridge.service.AiIntegrationService;
import com.healthbridge.service.SeverityAssessmentService;
import com.healthbridge.service.SymptomService;

import jakarta.transaction.Transactional;

@Service
//@RequiredArgsConstructor
public class SymptomServiceImpl implements SymptomService {

	@Autowired
	private SymptomRepository repo;

	@Autowired
	private AiIntegrationService ai;

	@Autowired
	private SeverityAssessmentService severityService;

	@Autowired
	private UserRepository userRepo;

	@Override
	@Transactional
	public SymptomRecord analyzeAndSave(Long userId, String symptoms, String language, Double lat, Double lon,
			String prompt) {
		var user = userRepo.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

		// String prompt = ai.buildPrompt(symptoms, language, lat, lon);
		String aiResponse = ai.generateSymptomAnalysis(prompt, language);

		SymptomRecord record = SymptomRecord.builder().user(user).symptoms(symptoms).aiResponse(aiResponse)
				.language(language).latitude(lat).longitude(lon).createdAt(java.time.LocalDateTime.now()).build();
		severityService.assess(record, aiResponse);
		return repo.save(record);
	}

	@Override
	public List<SymptomRecord> getByUser(Long userId) {
		return repo.findByUserId(userId); // Updated for relationship
	}

	@Override
	public List<SymptomRecord> getBySeverity(String level) {
		return repo.findBySeverityLevel(level);
	}
}