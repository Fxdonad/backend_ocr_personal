package com.ocrweb.backend_ocr.entity.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Document")
@JsonIgnoreProperties({"user", "file"})
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "fileID", nullable = true)
    private UploadedFiles file;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private String status;

    @Column
    private String base64;  // Lưu Base64 nếu là ảnh

    @Column(nullable = false)
    private String filePath;

    // Getters and Setters
    public Integer getDocumentID() {
        return documentID;
    }

    public void setDocumentID(Integer documentID) {
        this.documentID = documentID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UploadedFiles getFile() {
        return file;
    }

    public void setFile(UploadedFiles file) {
        this.file = file;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
