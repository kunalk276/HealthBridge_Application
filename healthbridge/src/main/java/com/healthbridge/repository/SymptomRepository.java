package com.healthbridge.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.healthbridge.entity.SymptomRecord;

@Repository
public interface SymptomRepository extends JpaRepository<SymptomRecord, Long> {
	List<SymptomRecord> findByUserId(Long userId);

	List<SymptomRecord> findBySeverityLevel(String level);

	// 🧩 Get top 5 most frequent symptoms for a user
	@Query("SELECT r.symptoms, COUNT(r) FROM SymptomRecord r WHERE r.user.id = :userId GROUP BY r.symptoms ORDER BY COUNT(r) DESC")
	List<Object[]> findTopSymptomsByUser(Long userId);

	// 📈 Get severity level counts for trend chart
	@Query("SELECT r.severityLevel, COUNT(r) FROM SymptomRecord r WHERE r.user.id = :userId GROUP BY r.severityLevel")
	List<Object[]> findSeverityTrendByUser(Long userId);
}