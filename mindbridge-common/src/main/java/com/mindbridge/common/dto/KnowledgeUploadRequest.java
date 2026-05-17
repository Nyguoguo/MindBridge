package com.mindbridge.common.dto;

public class KnowledgeUploadRequest {

    private String title;
    private String content;
    private String fileType;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
