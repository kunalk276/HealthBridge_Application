package com.healthbridge.dto;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomResponse {
	private Long id;
	private String symptoms;
	private String aiResponse;
	private String language;
	private String severityLevel;
	private Double severityScore; // ✅ Must exist
	private Boolean referralNeeded; // ✅ Must exist
	private Double latitude;
	private Double longitude;
	private LocalDateTime createdAt;
}