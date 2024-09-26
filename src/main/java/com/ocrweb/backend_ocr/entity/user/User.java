package com.ocrweb.backend_ocr.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocrweb.backend_ocr.entity.actions.UserActions;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.profile.UserProfile;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users")
@JsonIgnoreProperties({"uploadedFiles", "processingLogs", "userActions"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private long currentGP = 100;

    @Column(nullable = false)
    private long maxStorage = 10 * 1024 * 1024;

    @Column(name = "usedRequests", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int usedRequests;

    @Column(name = "remainingRequests", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int remainingRequests;

    @Column(name = "upgradedRequests", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int upgradedRequests;

    @Column(name = "totalRequests", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int totalRequests;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private LocalDateTime lastLoginDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UploadedFiles> uploadedFiles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserActions> userActions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProcessingLog> processingLogs;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private UserProfile userProfile;

    // Getters and Setters
    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCurrentGP() {
        return currentGP;
    }

    public void setCurrentGP(long currentGP) {
        this.currentGP = currentGP;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<UploadedFiles> getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(List<UploadedFiles> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public List<UserActions> getUserActions() {
        return userActions;
    }

    public void setUserActions(List<UserActions> userActions) {
        this.userActions = userActions;
    }

    public List<ProcessingLog> getProcessingLogs() {
        return processingLogs;
    }

    public void setProcessingLogs(List<ProcessingLog> processingLogs) {
        this.processingLogs = processingLogs;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public long getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(long maxStorage) {
        this.maxStorage = maxStorage;
    }

    // Getters and Setters

    public int getUsedRequests() {
        return usedRequests;
    }

    public void setUsedRequests(int usedRequests) {
        this.usedRequests = usedRequests;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public int getUpgradedRequests() {
        return upgradedRequests;
    }

    public void setUpgradedRequests(int upgradedRequests) {
        this.upgradedRequests = upgradedRequests;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public void incrementRequests(int requests) {
        this.usedRequests += requests;
        this.remainingRequests -= requests;
    }

    public void upgradeRequests(int additionalRequests) {
        this.remainingRequests += additionalRequests;
        this.upgradedRequests += additionalRequests;
        this.totalRequests += additionalRequests;
    }

}
