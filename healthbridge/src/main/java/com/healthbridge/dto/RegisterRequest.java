package com.healthbridge.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RegisterRequest {
	@NotBlank
	@Size(min = 3, max = 50)
	private String username;

	@NotBlank
	@Size(min = 6, max = 100)
	private String password;

	// @NotBlank
	// private String role; // USER or ADMIN

	@Pattern(regexp = "^$|^[a-z]{2}$", message = "Language must be empty or 2-letter code")
	private String language;

	@Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone must be 10 digits or empty")
	private String phone;
//	@NotBlank(message = "Area is required")
	@Size(min = 2, max = 30, message = "Area must be between 2 and 30 characters")
	@Pattern(regexp = "^[A-Za-z\\s]+$", message = "Area must contain only letters and spaces")
	private String area;
	private String role = "ROLE_USER"; // default
	// @NotBlank(message = "City is required")
	@Size(min = 2, max = 30, message = "City must be between 2 and 30 characters")
	@Pattern(regexp = "^[A-Za-z\\s]+$", message = "City must contain only letters and spaces")
	private String city;

	// @NotBlank(message = "State is required")
	@Size(min = 2, max = 30, message = "State must be between 2 and 30 characters")
	@Pattern(regexp = "^[A-Za-z\\s]+$", message = "State must contain only letters and spaces")
	@JsonProperty("state")
	private String state;

}