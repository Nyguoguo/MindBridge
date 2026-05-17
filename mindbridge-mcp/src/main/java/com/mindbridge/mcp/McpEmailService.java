package com.mindbridge.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class McpEmailService {

    private static final Logger log = LoggerFactory.getLogger(McpEmailService.class);

    private final JavaMailSender mailSender;
    private final String alertRecipient;
    private final boolean enabled;

    public McpEmailService(
            @Value("${mindbridge.email.alert-to:admin@mindbridge.edu}") String alertRecipient,
            @Value("${mindbridge.email.enabled:false}") boolean enabled,
            @Autowired(required = false) JavaMailSender mailSender) {
        this.alertRecipient = alertRecipient;
        this.enabled = enabled;
        this.mailSender = mailSender;
    }

    public boolean sendAlert(String studentName, String riskLevel, String keywords, String summary) {
        if (!enabled || mailSender == null) {
            log.info("Email alert would be sent to {}: student={}, level={}, keywords={}",
                    alertRecipient, studentName, riskLevel, keywords);
            return true;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(alertRecipient);
            msg.setSubject("[MindBridge Alert] High Risk Student: " + riskLevel);
            msg.setText(String.format("""
                    MindBridge Psychological Alert
                    ================================
                    Student: %s
                    Risk Level: %s
                    Detected Keywords: %s
                    Summary: %s

                    Suggested Action: Immediate psychological intervention required.
                    Please contact the student as soon as possible.

                    This is an automated alert from MindBridge system.
                    """, studentName, riskLevel, keywords, summary));
            mailSender.send(msg);
            log.info("Alert email sent to {} for student {}", alertRecipient, studentName);
            return true;
        } catch (Exception e) {
            log.error("Failed to send alert email: {}", e.getMessage());
            return false;
        }
    }
}
