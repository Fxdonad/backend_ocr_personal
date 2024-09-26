package com.ocrweb.backend_ocr.repository.logs;

import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Integer> {
    List<ProcessingLog> findByUploadedFile(UploadedFiles uploadedFile);
}

