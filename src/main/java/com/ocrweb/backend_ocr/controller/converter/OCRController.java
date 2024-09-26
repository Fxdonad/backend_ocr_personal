package com.ocrweb.backend_ocr.controller.converter;

import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.converter.Base64ToTextService;
import com.ocrweb.backend_ocr.service.documents.DocumentService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ocr")
public class OCRController {

    @Autowired
    private Base64ToTextService base64ToTextService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/convertBase64ToText")
    public ResponseEntity<String> convertBase64ToText(@RequestHeader("Authorization") String token,
                                                      @RequestBody Map<String, String> requestBody) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        if (user.getRemainingRequests() <= 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OCR request limit reached. Please upgrade your plan.");
        }

        // Deduct 1 request and increment used requests
        user.incrementRequests(1);
        userService.updateUser(user);

        try {
            String base64Content = requestBody.get("contentBase64");
            String resultText = base64ToTextService.convertBase64ToText(base64Content, user);
            return ResponseEntity.ok(resultText);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing OCR: " + e.getMessage());
        }
    }

    @PostMapping("/convertSelectedFilesToText")
    public ResponseEntity<?> convertSelectedFilesToText(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String, List<Integer>> requestBody) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        List<Integer> documentIds = requestBody.get("documentIds");

        if (documentIds == null || documentIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No document IDs provided.");
        }

        int requiredRequests = documentIds.size();

        if (user.getRemainingRequests() < requiredRequests) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not enough OCR requests remaining. Please upgrade your plan.");
        }

        try {
            List<Document> documents = documentService.getDocumentsByIdsAndUser(documentIds, user);

            List<Map<String, Object>> responseList = documents.stream().map(document -> {
                String resultText = base64ToTextService.convertBase64ToText(document.getBase64(), user);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("DocumentID", document.getDocumentID());
                resultMap.put("FileID", document.getFile().getFileID());
                resultMap.put("OCRText", resultText);
                return resultMap;
            }).collect(Collectors.toList());

            // Deduct the required requests and increment used requests
            user.incrementRequests(requiredRequests);
            userService.updateUser(user);

            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing OCR: " + e.getMessage());
        }
    }
}
