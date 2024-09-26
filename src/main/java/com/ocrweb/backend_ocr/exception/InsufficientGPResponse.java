package com.ocrweb.backend_ocr.exception;

public class InsufficientGPResponse {
    private String message;
    private Object details;

    public InsufficientGPResponse(String message, Object details) {
        this.message = message;
        this.details = details;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}