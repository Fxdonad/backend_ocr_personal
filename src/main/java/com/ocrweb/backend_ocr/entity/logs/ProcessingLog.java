package com.ocrweb.backend_ocr.entity.logs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ProcessingLogs")
@JsonIgnoreProperties({"user", "uploadedFile"})
public class ProcessingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logID;

    @ManyToOne
    @JoinColumn(name = "fileID", nullable = true)
    private UploadedFiles uploadedFile;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(nullable = false)
    private String actionType;

    @Column(length = 1000)
    private String actionDetails;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    @Column(nullable = false)
    private Boolean isSuccess = true;

    @Column(length = 1000)
    private String errorMessage;


    // Getters and Setters
    public Integer getLogID() {
        return logID;
    }

    public void setLogID(Integer logID) {
        this.logID = logID;
    }

    public UploadedFiles getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFiles uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionDetails() {
        return actionDetails;
    }

    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}


