package com.ocrweb.backend_ocr.dto;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String username;
    private long currentGP;
    private Long gpUsed;
    private LocalDateTime transactionDate;
    private String transactionStatus;

    // Constructor
    public TransactionResponse(String username, long currentGP, Long gpUsed, LocalDateTime transactionDate, String transactionStatus) {
        this.username = username;
        this.currentGP = currentGP;
        this.gpUsed = gpUsed;
        this.transactionDate = transactionDate;
        this.transactionStatus = transactionStatus;
    }


    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getCurrentGP() {
        return currentGP;
    }

    public void setCurrentGP(long currentGP) {
        this.currentGP = currentGP;
    }

    public long getGpUsed() {
        return gpUsed;
    }

    public void setGpUsed(long gpUsed) {
        this.gpUsed = gpUsed;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
