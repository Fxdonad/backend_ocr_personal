package com.ocrweb.backend_ocr.entity.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "FolderFiles")
@JsonIgnoreProperties("folderUploads")
public class FolderFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folderFileID;

    @ManyToOne
    @JoinColumn(name = "folderID", nullable = false)
    private FolderUploads folderUploads;

    @ManyToOne
    @JoinColumn(name = "fileID", nullable = false)
    private UploadedFiles uploadedFiles;

    // Getters and Setters
    public Integer getFolderFileID() {
        return folderFileID;
    }

    public void setFolderFileID(Integer folderFileID) {
        this.folderFileID = folderFileID;
    }

    public UploadedFiles getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(UploadedFiles uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public FolderUploads getFolderUploads() {
        return folderUploads;
    }

    public void setFolderUploads(FolderUploads folderUploads) {
        this.folderUploads = folderUploads;
    }
}
