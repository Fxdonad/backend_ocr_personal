package com.ocrweb.backend_ocr.service.actions;

import com.ocrweb.backend_ocr.entity.actions.UserActions;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.actions.UserActionsRepository;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActionService {

    @Autowired
    private UserActionsRepository userActionsRepository;

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    public void logUserAction(User user, Document document, String actionType, boolean isSuccess) {
        if (document == null) {
            // Thay đổi: Chỉ ghi log cho ProcessingLog nếu document là null
            ProcessingLog processingLog = new ProcessingLog();
            processingLog.setUser(user);
            processingLog.setUploadedFile(null); // Không có file liên kết
            processingLog.setActionType(actionType);
            processingLog.setActionDate(LocalDateTime.now());
            processingLog.setIsSuccess(isSuccess);
            processingLog.setErrorMessage(isSuccess ? null : "Error occurred during " + actionType);
            processingLogRepository.save(processingLog);
            return;
        }

        // Ghi log vào bảng UserActions
        UserActions userAction = new UserActions();
        userAction.setUser(user);
        userAction.setDocument(document);
        userAction.setActionType(actionType);
        userAction.setActionDate(LocalDateTime.now());

        userActionsRepository.save(userAction);

        // Ghi log vào bảng ProcessingLogs
        ProcessingLog processingLog = new ProcessingLog();
        processingLog.setUser(user);
        processingLog.setUploadedFile(document.getFile());
        processingLog.setActionType(actionType);
        processingLog.setActionDate(LocalDateTime.now());

        // Thiết lập isSuccess và errorMessage
        processingLog.setIsSuccess(isSuccess);
        processingLog.setErrorMessage(isSuccess ? null : "Error occurred during " + actionType);

        processingLogRepository.save(processingLog);
    }

    public List<UserActions> getUserLogs(User user) {
        return userActionsRepository.findByUser(user);
    }
}
