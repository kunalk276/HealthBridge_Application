package com.healthbridge.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.healthbridge.service.AiIntegrationService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AI Integration Service. Uses Google Gemini API to analyze
 * health symptoms and provide responses.
 */
@Service
@Slf4j

public class AiIntegrationServiceImpl implements AiIntegrationService {

	// Inject Gemini API key from application.properties file
	@Value("${gemini.api.key}")
	private String geminiApiKey;

	// Inject Gemini base API URL
	@Value("${gemini.api.url}")
	private String geminiApiUrl;

	// RestTemplate for making HTTP calls to Gemini API
	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Builds dynamic prompt based on user symptoms, selected language, and
	 * location.
	 */
	@Override
	public String buildPrompt(String symptoms, String language, Double lat, Double lon) {

		// Default language as Hindi if no language is provided
		String lang = (language == null || language.isBlank()) ? "Hindi" : language;

		// Add location only if coordinates are available
		String locationInfo = (lat != null && lon != null)
				? String.format("Location coordinates: %.2f, %.2f.", lat, lon)
				: "";

		// Returning formatted prompt template
		return String.format("""
				You are a compassionate healthcare assistant for Indian users.
				The user describes their health issue in %s: "%s"
				%s

				Please provide the following in %s:
				1️⃣ Simple explanation of possible cause.
				2️⃣ Three immediate 3 steps they should take.
				3️⃣ Based on severity and symptom keywords, recommend which specialist doctor to consult..
				4️⃣ Easy home remedies.
				5  Give response on maximum 80 words

				Be empathetic, simple, and avoid medical jargon.
				""", lang, symptoms, locationInfo, lang);
	}

	/**
	 * Sends prompt to Gemini and returns response text.
	 */
	@Override
	public String generateSymptomAnalysis(String prompt, String language) {
		return callGemini(prompt);
	}

	/**
	 * Calls Gemini API using REST request and extracts response text.
	 */
	@SuppressWarnings("unchecked")
	private String callGemini(String prompt) {
		try {
			// Construct final Gemini endpoint URL with API key
			String url = geminiApiUrl + "?key=" + geminiApiKey;

			// Setting request headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Building request payload structure expected by Gemini
			Map<String, Object> part = Map.of("text", prompt);
			Map<String, Object> content = Map.of("parts", List.of(part));
			Map<String, Object> body = Map.of("contents", List.of(content));

			// Creating HttpEntity object for POST request
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			// Make POST request to Gemini API
			ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

			// Extract response payload
			Map<String, Object> responseBody = response.getBody();

			// Handle empty response
			if (responseBody == null) {
				return "No response from Gemini AI.";
			}

			// Extract candidate output text
			List<?> candidates = (List<?>) responseBody.get("candidates");
			if (candidates != null && !candidates.isEmpty()) {

				Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
				Map<?, ?> contentMap = (Map<?, ?>) candidate.get("content");
				List<?> parts = (List<?>) contentMap.get("parts");

				// Extract final generated text
				if (parts != null && !parts.isEmpty()) {
					Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
					Object text = firstPart.get("text");

					if (text != null) {
						return text.toString().trim();
					}
				}
			}

			return "Unable to parse Gemini response.";
		} catch (Exception ex) {

			// Log detailed error in console
			log.error("Error calling Gemini API: {}", ex.getMessage());

			// Return user-friendly output
			return "Error connecting to Gemini AI service. Please check your API key or URL.";
		}
	}
}