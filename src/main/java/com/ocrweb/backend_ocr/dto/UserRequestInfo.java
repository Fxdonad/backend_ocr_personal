package com.ocrweb.backend_ocr.dto;

public class UserRequestInfo {
    private int totalRequests;
    private int remainingRequests;
    private int usedRequests;
    private int upgradedRequests;

    public UserRequestInfo(int totalRequests, int remainingRequests, int usedRequests, int upgradedRequests) {
        this.totalRequests = totalRequests;
        this.remainingRequests = remainingRequests;
        this.usedRequests = usedRequests;
        this.upgradedRequests = upgradedRequests;
    }

    // Getters and Setters
    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getRemainingRequests() {
        return remainingRequests;
    }

    public void setRemainingRequests(int remainingRequests) {
        this.remainingRequests = remainingRequests;
    }

    public int getUsedRequests() {
        return usedRequests;
    }

    public void setUsedRequests(int usedRequests) {
        this.usedRequests = usedRequests;
    }

    public int getUpgradedRequests() {
        return upgradedRequests;
    }

    public void setUpgradedRequests(int upgradedRequests) {
        this.upgradedRequests = upgradedRequests;
    }
}
