package com.healthbridge.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.healthbridge.entity.SymptomRecord;

@Repository
public interface SymptomRecordRepository extends JpaRepository<SymptomRecord, Long> {

	@Query("SELECT sr.severityLevel, COUNT(sr) FROM SymptomRecord sr GROUP BY sr.severityLevel")
	List<Object[]> countBySeverity();

	@Query("SELECT sr.symptoms, COUNT(sr) FROM SymptomRecord sr GROUP BY sr.symptoms")
	List<Object[]> countCommonSymptoms();

	@Query("SELECT sr.severityLevel, COUNT(sr) FROM SymptomRecord sr GROUP BY sr.severityLevel")
	List<Object[]> countSeveritySummary();

	@Query("SELECT sr.language, COUNT(sr) FROM SymptomRecord sr GROUP BY sr.language")
	List<Object[]> countLanguageWiseAi();

	@Query("SELECT sr.referralNeeded, COUNT(sr) FROM SymptomRecord sr GROUP BY sr.referralNeeded")
	List<Object[]> countReferralStats();
}