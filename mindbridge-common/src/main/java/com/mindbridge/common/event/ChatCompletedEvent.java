package com.mindbridge.common.event;

public class ChatCompletedEvent {

    private final Long sessionId;
    private final Long userId;
    private final String userContent;
    private final String assistantContent;

    public ChatCompletedEvent(Long sessionId, Long userId, String userContent, String assistantContent) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userContent = userContent;
        this.assistantContent = assistantContent;
    }

    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public String getUserContent() { return userContent; }
    public String getAssistantContent() { return assistantContent; }
}
