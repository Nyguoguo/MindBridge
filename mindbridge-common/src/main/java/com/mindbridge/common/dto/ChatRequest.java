package com.mindbridge.common.dto;

public class ChatRequest {

    private String content;
    private String model;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
