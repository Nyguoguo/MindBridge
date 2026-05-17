package com.mindbridge.mcp;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RiskDetector {

    private static final Set<String> HIGH_RISK_KEYWORDS = Set.of(
            "自杀", "自残", "不想活", "结束生命", "死掉", "自伤", "割腕",
            "suicide", "kill myself", "end my life"
    );

    private static final Set<String> MEDIUM_RISK_KEYWORDS = Set.of(
            "绝望", "无助", "崩溃", "焦虑到受不了", "睡不着",
            "hopeless", "helpless", "anxiety", "panic"
    );

    private static final Set<String> LOW_RISK_KEYWORDS = Set.of(
            "失眠", "焦虑", "抑郁", "压力大", "心情差", "烦躁",
            "stress", "depression", "insomnia", "sad"
    );

    public RiskResult analyze(String content) {
        String lower = content.toLowerCase();
        int score = 0;
        StringBuilder keywords = new StringBuilder();

        for (String kw : HIGH_RISK_KEYWORDS) {
            if (lower.contains(kw)) { score += 30; keywords.append(kw).append(","); }
        }
        for (String kw : MEDIUM_RISK_KEYWORDS) {
            if (lower.contains(kw)) { score += 15; keywords.append(kw).append(","); }
        }
        for (String kw : LOW_RISK_KEYWORDS) {
            if (lower.contains(kw)) { score += 5; keywords.append(kw).append(","); }
        }

        String level;
        String suggestion;
        if (score >= 30) {
            level = "HIGH";
            suggestion = "紧急干预！建议立即联系心理咨询师进行面对面评估。可联系24小时心理危机热线。";
        } else if (score >= 15) {
            level = "MEDIUM";
            suggestion = "建议安排心理咨询。推荐进行正念减压训练和认知行为疗法。";
        } else if (score >= 5) {
            level = "LOW";
            suggestion = "建议关注学生状态，定期随访。推荐参加心理健康工作坊。";
        } else {
            level = "NORMAL";
            suggestion = "";
        }

        String kw = !keywords.isEmpty() ? keywords.substring(0, keywords.length() - 1) : "";
        return new RiskResult(level, score, kw, suggestion);
    }

    public record RiskResult(String level, int score, String keywords, String suggestion) {}
}
