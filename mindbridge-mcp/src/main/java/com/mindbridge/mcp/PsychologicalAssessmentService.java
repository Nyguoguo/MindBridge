package com.mindbridge.mcp;

import com.mindbridge.common.entity.PsychologicalRecord;
import com.mindbridge.common.repository.PsychologicalRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PsychologicalAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(PsychologicalAssessmentService.class);

    private final PsychologicalRecordRepository recordRepo;

    public PsychologicalAssessmentService(PsychologicalRecordRepository recordRepo) {
        this.recordRepo = recordRepo;
    }

    public Map<String, Object> getAssessmentStats() {
        List<PsychologicalRecord> records = recordRepo.findAll();
        long total = records.size();
        long high = records.stream().filter(r -> "HIGH".equals(r.getRiskLevel())).count();
        long medium = records.stream().filter(r -> "MEDIUM".equals(r.getRiskLevel())).count();
        long low = records.stream().filter(r -> "LOW".equals(r.getRiskLevel())).count();
        long normal = records.stream().filter(r -> "NORMAL".equals(r.getRiskLevel())).count();

        double avgScore = records.stream().mapToInt(r -> r.getRiskScore() != null ? r.getRiskScore() : 0)
                .average().orElse(0);

        return Map.of(
                "totalRecords", total,
                "highRisk", high,
                "mediumRisk", medium,
                "lowRisk", low,
                "normal", normal,
                "averageRiskScore", Math.round(avgScore * 100.0) / 100.0
        );
    }

    public Map<String, List<String>> getTopKeywords(int limit) {
        return Map.of("topKeywords", recordRepo.findAll().stream()
                .filter(r -> r.getKeywords() != null && !r.getKeywords().isEmpty())
                .flatMap(r -> List.of(r.getKeywords().split(",")).stream())
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .collect(Collectors.groupingBy(k -> k, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.toList()));
    }
}
