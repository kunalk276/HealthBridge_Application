package com.healthbridge.dto;


import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserOverview {
	private long totalUsers;
	private Map<String, Long> usersByState;
	private Map<String, Long> usersByCity;
	private Map<String, Long> usersByLanguage;
	private List<String> recentUsers;
}