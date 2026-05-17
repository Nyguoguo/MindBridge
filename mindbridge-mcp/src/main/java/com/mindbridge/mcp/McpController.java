package com.mindbridge.mcp;

import com.mindbridge.common.entity.PsychologicalRecord;
import com.mindbridge.common.repository.PsychologicalRecordRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpExcelService excelService;
    private final McpEmailService emailService;
    private final PsychologicalRecordRepository recordRepo;
    private final PsychologicalAssessmentService assessmentService;

    public McpController(McpExcelService excelService, McpEmailService emailService,
                         PsychologicalRecordRepository recordRepo,
                         PsychologicalAssessmentService assessmentService) {
        this.excelService = excelService;
        this.emailService = emailService;
        this.recordRepo = recordRepo;
        this.assessmentService = assessmentService;
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, String>> exportRecords() {
        return Mono.fromCallable(() -> {
            String path = excelService.exportRecords();
            return Map.of("file", path, "status", "success");
        });
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<List<PsychologicalRecord>> getRecords() {
        return Mono.fromCallable(recordRepo::findAll);
    }

    @GetMapping("/records/high-risk")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<List<PsychologicalRecord>> getHighRiskRecords() {
        return Mono.fromCallable(() -> recordRepo.findByRiskLevelAndAlertSent("HIGH", false));
    }

    @PostMapping("/alert")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, String>> sendAlert(@RequestBody Map<String, String> body) {
        return Mono.fromCallable(() -> {
            boolean sent = emailService.sendAlert(
                    body.getOrDefault("studentName", "Unknown"),
                    body.getOrDefault("riskLevel", "HIGH"),
                    body.getOrDefault("keywords", ""),
                    body.getOrDefault("summary", ""));
            return Map.of("status", sent ? "sent" : "failed");
        });
    }

    @GetMapping("/export-dir")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, String>> getExportDir() {
        return Mono.fromCallable(() -> Map.of("directory", excelService.getExportDir()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, Object>> getStats() {
        return Mono.fromCallable(assessmentService::getAssessmentStats);
    }

    @GetMapping("/keywords")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, java.util.List<String>>> getKeywords() {
        return Mono.fromCallable(() -> assessmentService.getTopKeywords(10));
    }
}
