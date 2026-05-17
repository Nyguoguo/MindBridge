package com.mindbridge.mcp;

import com.mindbridge.common.event.ChatCompletedEvent;
import com.mindbridge.common.entity.PsychologicalRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RiskAnalysisListener {

    private static final Logger log = LoggerFactory.getLogger(RiskAnalysisListener.class);

    private final RiskMonitorService riskMonitor;

    public RiskAnalysisListener(RiskMonitorService riskMonitor) {
        this.riskMonitor = riskMonitor;
    }

    @EventListener
    public void onChatCompleted(ChatCompletedEvent event) {
        PsychologicalRecord record = riskMonitor.analyzeAndRecord(
                event.getSessionId(), event.getUserId(), event.getUserContent());
        log.info("Risk analyzed: session={}, level={}, score={}",
                event.getSessionId(), record.getRiskLevel(), record.getRiskScore());
    }
}
