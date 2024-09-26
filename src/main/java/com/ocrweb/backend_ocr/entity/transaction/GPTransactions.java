package com.ocrweb.backend_ocr.entity.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Entity
public class GPTransactions {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer GPTransactionID;  // Primary key
    private Integer userID;  // ID of the user associated with this transaction
    private Long gpUsed;  // Amount of GP used in the transaction
    private String transactionStatus;  // Status of the transaction (e.g., "Success", "Failed")
    private LocalDateTime transactionDate;  // Date and time when the transaction occurred

    // Getters and Setters for each field
    public Integer getId() {
        return GPTransactionID;
    }

    public void setId(Integer GPTransactionID) {
        this.GPTransactionID = GPTransactionID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public long getGpUsed() {
        return gpUsed;
    }

    public void setGpUsed(long gpUsed) {
        this.gpUsed = gpUsed;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
