package com.ocrweb.backend_ocr.entity.actions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.documents.Document;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "UserActions")
@JsonIgnoreProperties({"user", "document"})
public class UserActions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actionID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "documentID", nullable = false)
    private Document document;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    // Getters and Setters
    public Integer getActionID() {
        return actionID;
    }

    public void setActionID(Integer actionID) {
        this.actionID = actionID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDateTime actionDate) {
        this.actionDate = actionDate;
    }
}
