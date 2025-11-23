package com.healthbridge.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "symptom_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymptomRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String symptoms;

	@Column(columnDefinition = "TEXT")
	private String aiResponse;
	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;
	// @Column(length = 10)
	// private SeverityLevel severityLevel; // enum reference

	private double severityScore;
	private boolean referralNeeded;

	@Column(length = 6)
	private String language;
	// ✅ Keep severity as String (not enum)
	@Column(name = "severity_level", length = 12)
	private String severityLevel; // e.g., "LOW", "MEDIUM", "HIGH", "EMERGENCY"

	private LocalDateTime createdAt;

	// 🔗 Relation with User
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

//	public enum SeverityLevel {
//		LOW, MEDIUM, HIGH
//	}

}