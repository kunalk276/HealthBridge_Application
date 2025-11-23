package com.healthbridge.service;


import com.healthbridge.entity.SymptomRecord;

public interface SeverityAssessmentService {
	void assess(SymptomRecord record, String aiResponse);

}
