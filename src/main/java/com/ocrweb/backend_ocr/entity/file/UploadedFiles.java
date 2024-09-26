package com.ocrweb.backend_ocr.entity.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "UploadedFiles")
@JsonIgnoreProperties({"user", "processingLogs"})
public class UploadedFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileID;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column
    private String base64;

    @Column(nullable = false)
    private Boolean processed = false;

    private LocalDateTime processedDate;

    @OneToOne(mappedBy = "file", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Document document;

    @OneToMany(mappedBy = "uploadedFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProcessingLog> processingLogs;

    @PreRemove
    private void preRemove() {
        if (processingLogs != null) {
            for (ProcessingLog log : processingLogs) {
                log.setUploadedFile(null);  // Set the uploadedFile to null before file deletion
            }
        }
    }

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String thumbnail;

    // Getters and Setters
    public Integer getFileID() {
        return fileID;
    }

    public void setFileID(Integer fileID) {
        this.fileID = fileID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public List<ProcessingLog> getProcessingLogs() {
        return processingLogs;
    }

    public void setProcessingLogs(List<ProcessingLog> processingLogs) {
        this.processingLogs = processingLogs;
    }

    public String getBase64() {
        return base64;
    }
    public void setBase64(String base64) {
        this.base64 = base64;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
