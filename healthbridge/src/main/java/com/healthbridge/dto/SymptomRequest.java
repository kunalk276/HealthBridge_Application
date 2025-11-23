package com.healthbridge.dto;


import jakarta.validation.constraints.NotBlank;
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
public class SymptomRequest {
	@NotBlank
	private String symptoms;
	private String language; // optional override
	private Double latitude;
	private Double longitude;
}