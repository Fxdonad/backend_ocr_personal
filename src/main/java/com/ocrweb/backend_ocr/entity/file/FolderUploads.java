package com.ocrweb.backend_ocr.entity.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ocrweb.backend_ocr.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "FolderUploads")
@JsonIgnoreProperties("user")
public class FolderUploads {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folderID;

    @OneToMany(mappedBy = "folderUploads", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FolderFiles> folderFiles;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "folderName", nullable = false)
    private String folderName;

    @Column(name = "uploadDate", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "processedDate")
    private LocalDateTime processedDate;

    // Getters and Setters
    public Integer getFolderID() {
        return folderID;
    }

    public void setFolderID(Integer folderID) {
        this.folderID = folderID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public List<FolderFiles> getFolderFiles() {
        return folderFiles;
    }

    public void setFolderFiles(List<FolderFiles> folderFiles) {
        this.folderFiles = folderFiles;
    }

}
