package com.healthbridge.service;


public interface AiIntegrationService {

	String generateSymptomAnalysis(String prompt, String language);

	String buildPrompt(String symptoms, String language, Double lat, Double lon);
}
