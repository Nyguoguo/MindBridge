package com.mindbridge.mcp;

import com.mindbridge.common.entity.PsychologicalRecord;
import com.mindbridge.common.repository.PsychologicalRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskMonitorService {

    private static final Logger log = LoggerFactory.getLogger(RiskMonitorService.class);

    private final RiskDetector detector;
    private final McpEmailService emailService;
    private final PsychologicalRecordRepository recordRepo;

    public RiskMonitorService(RiskDetector detector, McpEmailService emailService,
                              PsychologicalRecordRepository recordRepo) {
        this.detector = detector;
        this.emailService = emailService;
        this.recordRepo = recordRepo;
    }

    public PsychologicalRecord analyzeAndRecord(Long sessionId, Long userId, String content) {
        RiskDetector.RiskResult result = detector.analyze(content);

        PsychologicalRecord record = new PsychologicalRecord();
        record.setSessionId(sessionId);
        record.setUserId(userId != null ? userId : 1L);
        record.setRiskLevel(result.level());
        record.setRiskScore(result.score());
        record.setKeywords(result.keywords());
        record.setSummary(content.length() > 200 ? content.substring(0, 200) : content);
        record.setSuggestion(result.suggestion());
        record = recordRepo.save(record);

        if ("HIGH".equals(result.level())) {
            boolean sent = emailService.sendAlert(
                    "Student-" + (userId != null ? userId : "unknown"),
                    result.level(), result.keywords(), record.getSummary());
            record.setAlertSent(sent);
            record = recordRepo.save(record);
            log.warn("HIGH risk detected in session {}, alert sent: {}", sessionId, sent);
        }

        return record;
    }
}
