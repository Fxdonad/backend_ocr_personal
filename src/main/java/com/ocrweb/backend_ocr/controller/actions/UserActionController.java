package com.ocrweb.backend_ocr.controller.actions;

import com.ocrweb.backend_ocr.entity.actions.UserActions;
import com.ocrweb.backend_ocr.entity.logs.ActionLogRequest;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.service.actions.UserActionService;
import com.ocrweb.backend_ocr.service.documents.DocumentService;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/user/actions")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProcessingLogService processingLogService;

    @PostMapping("/log")
    public ResponseEntity<?> logAction(@RequestHeader("Authorization") String token,
                                       @RequestBody ActionLogRequest request) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);
        String[] validActionTypes = {"Upload", "Download", "Delete"};

        if (!Arrays.asList(validActionTypes).contains(request.getActionType())) {
            return ResponseEntity.badRequest().body("Invalid action type.");
        }

        Document document = documentService.getDocumentByIdAndUser(request.getDocumentId(), user);

        // Ghi log hành động của user
        userActionService.logUserAction(user, document, request.getActionType(), request.isSuccess());

        return ResponseEntity.ok("Action and ProcessingLog logged successfully.");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<UserActions>> getUserLogs(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        List<UserActions> userLogs = userActionService.getUserLogs(user);

        return ResponseEntity.ok(userLogs);
    }
}
