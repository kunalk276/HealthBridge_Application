package com.healthbridge.controller;


import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthbridge.dto.ApiResult;
import com.healthbridge.dto.SymptomRequest;
import com.healthbridge.dto.SymptomResponse;
import com.healthbridge.entity.SymptomRecord;
import com.healthbridge.entity.User;
import com.healthbridge.service.SymptomService;
import com.healthbridge.service.UserService;

import jakarta.validation.Valid;

//@RestController
//@RequestMapping("/api/symptoms")
//@CrossOrigin(origins = "http://localhost:4200")
//public class SymptomController {
//
//	@Autowired
//	private SymptomService symptomService;
//
//	@Autowired
//	private UserService userService;
//
//	/**
//	 * 🔍 Analyze user symptoms and return AI-generated suggestions
//	 */
//	@PostMapping("/analyze")
//	public ResponseEntity<ApiResult<?>> analyze(@Valid @RequestBody SymptomRequest req, Principal principal) {
//
//		if (principal == null) {
//			return ResponseEntity.status(401).body(ApiResult.fail("Not authenticated"));
//		}
//
//		// Support both Optional<User> and direct User return types
//		User user = null;
//		Object result = userService.findByUsername(principal.getName());
//
//		if (result instanceof Optional) {
//			Optional<?> userOpt = (Optional<?>) result;
//			if (userOpt.isEmpty()) {
//				return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
//			}
//			user = (User) userOpt.get();
//		} else if (result instanceof User) {
//			user = (User) result;
//		}
//
//		if (user == null) {
//			return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
//		}
//
//		// Determine language
//		String language = (req.getLanguage() != null && !req.getLanguage().isBlank()) ? req.getLanguage()
//				: user.getLanguage();
//		if (language == null)
//			language = "en";
//
//		// Call service
//		SymptomRecord record = symptomService.analyzeAndSave(user.getId(), req.getSymptoms(), language,
//				req.getLatitude(), req.getLongitude());
//
//		SymptomResponse resp = map(record);
//		return ResponseEntity.ok(ApiResult.ok(resp));
//	}
//
//	/**
//	 * 📜 Get the user's previous symptom analysis history
//	 */
//	@GetMapping("/history")
//	public ResponseEntity<ApiResult<?>> history(Principal principal) {
//
//		if (principal == null) {
//			return ResponseEntity.status(401).body(ApiResult.fail("Not authenticated"));
//		}
//
//		Optional<User> userOpt = userService.findByUsername(principal.getName());
//		if (userOpt.isEmpty()) {
//			return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
//		}
//		User user = userOpt.get();
//
//		List<SymptomResponse> items = symptomService.getByUser(user.getId()).stream().map(this::map)
//				.collect(Collectors.toList());
//
//		return ResponseEntity.ok(ApiResult.ok(items));
//	}
//
//	/**
//	 * 🧭 Map entity to response DTO
//	 */
//	private SymptomResponse map(SymptomRecord r) {
//		return SymptomResponse.builder().id(r.getId()).symptoms(r.getSymptoms()).aiResponse(r.getAiResponse())
//				.language(r.getLanguage()).severityLevel(r.getSeverityLevel()).severityScore(r.getSeverityScore())
//				.referralNeeded(r.isReferralNeeded()).latitude(r.getLatitude()).longitude(r.getLongitude())
//				.createdAt(r.getCreatedAt()).build();
//	}
//}*/
@RestController
@RequestMapping("/api/symptoms")
@CrossOrigin(origins = "http://localhost:4200")
public class SymptomController {

	@Autowired
	private UserService userService;

	@Autowired
	private SymptomService symptomService;

	/**
	 * Analyze user symptoms and return AI-generated suggestions
	 */
	@PostMapping("/analyze")
	public ResponseEntity<ApiResult<?>> analyze(@Valid @RequestBody SymptomRequest req, Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(401).body(ApiResult.fail("Not authenticated"));
		}

		// Fetch user from database
		User user = null;
		Object result = userService.findByUsername(principal.getName());

		if (result instanceof Optional) {
			Optional<?> userOpt = (Optional<?>) result;
			if (userOpt.isEmpty()) {
				return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
			}
			user = (User) userOpt.get();
		} else if (result instanceof User) {
			user = (User) result;
		}

		if (user == null) {
			return ResponseEntity.status(404).body(ApiResult.fail("User not found"));
		}

		// Determine language: request language > user's saved language > default
		// English
		String language = (req.getLanguage() != null && !req.getLanguage().isBlank()) ? req.getLanguage()
				: user.getLanguage();
		if (language == null)
			language = "en";

		// AI Prompt
		String prompt = String.format("""
				The user speaks %s language.
				Their reported symptoms are: %s
				Please reply in %s language with:
				1️ A simple explanation of possible cause.
				2️ Three immediate steps to take.
				3️ Which specialist doctor to consult.
				4️ Easy home remedies.
				Keep response under 80 words.
				""", language, req.getSymptoms(), language);

		// Process & save record
		SymptomRecord record = symptomService.analyzeAndSave(user.getId(), req.getSymptoms(), language,
				req.getLatitude(), req.getLongitude(), prompt // send prompt for logging or debugging
		);

		SymptomResponse resp = map(record);
		return ResponseEntity.ok(ApiResult.ok(resp));
	}

	// Utility mapper (convert record to response DTO)
	private SymptomResponse map(SymptomRecord record) {
		return SymptomResponse.builder().id(record.getId()).aiResponse(record.getAiResponse())
				.severityLevel(record.getSeverityLevel()).referralNeeded(record.isReferralNeeded())
				.createdAt(record.getCreatedAt()).language(record.getLanguage()).build();
	}
}