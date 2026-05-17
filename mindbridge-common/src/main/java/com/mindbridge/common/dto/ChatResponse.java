package com.mindbridge.common.dto;

public class ChatResponse {

    private Long messageId;
    private String content;

    public ChatResponse() {}

    public ChatResponse(Long messageId, String content) {
        this.messageId = messageId;
        this.content = content;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
