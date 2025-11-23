package com.healthbridge.dto;


import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegionalHealth {
	private Map<String, Map<String, Long>> severityByState;
	private List<String> highSeverityHotspots;
}