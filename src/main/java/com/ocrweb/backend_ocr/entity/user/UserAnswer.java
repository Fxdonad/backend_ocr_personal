package com.ocrweb.backend_ocr.entity.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "UserAnswer")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    @JsonIgnore // Prevent User entity from being included in the JSON response
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestionID", nullable = false)
    @JsonBackReference
    private UserQuestion question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private LocalDateTime answerTime;


    // Constructors, Getters, and Setters
    public UserAnswer() {}

    public UserAnswer(User user, UserQuestion question, String answer, LocalDateTime answerTime) {
        this.user = user;
        this.question = question;
        this.answer = answer;
        this.answerTime = answerTime;
    }

    public Integer getAnswerID() {
        return answerID;
    }

    public void setAnswerID(Integer answerID) {
        this.answerID = answerID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserQuestion getQuestion() {
        return question;
    }

    public void setQuestion(UserQuestion question) {
        this.question = question;
    }

    public Integer getQuestionID() {
        return question != null ? question.getMessageID() : null;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }
}
