package com.ocrweb.backend_ocr.dto;

public class UserGP {

    private String username;
    private long currentGP;

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

    public UserGP(String username, long currentGP) {
        this.username = username;
        this.currentGP = currentGP;
    }
}