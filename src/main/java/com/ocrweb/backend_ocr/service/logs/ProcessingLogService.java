package com.ocrweb.backend_ocr.service.logs;

import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProcessingLogService {

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    public void logProcessing(UploadedFiles file, User user, String actionType, boolean isSuccess, String errorMessage) {
        ProcessingLog log = new ProcessingLog();
        log.setUploadedFile(file);
        log.setUser(user);
        log.setActionType(actionType);
        log.setActionDate(LocalDateTime.now());
        log.setIsSuccess(isSuccess);
        log.setErrorMessage(errorMessage);
        processingLogRepository.save(log);
    }

    public void saveProcessingLog(ProcessingLog log) {
        processingLogRepository.save(log);
    }
}
