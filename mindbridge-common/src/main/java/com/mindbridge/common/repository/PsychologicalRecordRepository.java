package com.mindbridge.common.repository;

import com.mindbridge.common.entity.PsychologicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychologicalRecordRepository extends JpaRepository<PsychologicalRecord, Long> {
    List<PsychologicalRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PsychologicalRecord> findByRiskLevelAndAlertSent(String riskLevel, Boolean alertSent);
}
