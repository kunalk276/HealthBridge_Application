package com.healthbridge.service;


import java.util.List;

import com.healthbridge.entity.SymptomRecord;

public interface SymptomService {
	SymptomRecord analyzeAndSave(Long userId, String symptoms, String language, Double lat, Double lon, String prompt);

	List<SymptomRecord> getByUser(Long userId);

	List<SymptomRecord> getBySeverity(String level);
}