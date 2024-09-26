package com.ocrweb.backend_ocr.entity.logs;

public class ActionLogRequest {
    private Integer documentId;
    private String actionType;
    private boolean isSuccess;

    // Getters và Setters

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean success) {
        isSuccess = success;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}
