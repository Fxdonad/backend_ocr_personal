package com.ocrweb.backend_ocr.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "UserQuestion")
public class UserQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    @   JsonIgnore // Prevent User data from being included in the JSON response
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime messageTime;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserAnswer> answers;

    @Column(name = "UserID", insertable = false, updatable = false)
    private Integer userID;

    // Constructors, Getters, and Setters
    public UserQuestion() {}

    public UserQuestion(User user, String message, LocalDateTime messageTime) {
        this.user = user;
        this.message = message;
        this.messageTime = messageTime;
    }

    public Integer getMessageID() {
        return messageID;
    }

    public void setMessageID(Integer messageID) {
        this.messageID = messageID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getUserID() {
        return user != null ? user.getUserID() : null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }

    public List<UserAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<UserAnswer> answers) {
        this.answers = answers;
    }
}
