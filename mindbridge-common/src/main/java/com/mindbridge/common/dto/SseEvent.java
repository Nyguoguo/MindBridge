package com.mindbridge.common.dto;

public class SseEvent {

    private String token;
    private boolean done;
    private Long messageId;

    public SseEvent() {}

    public SseEvent(String token, boolean done) {
        this.token = token;
        this.done = done;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
}
