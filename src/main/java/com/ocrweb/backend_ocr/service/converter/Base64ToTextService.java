package com.ocrweb.backend_ocr.service.converter;

import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class Base64ToTextService {

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    private final String OCR_API_URL = "http://34.143.142.24/api/ocrbase64?crop=true&printedTextDetection=true&ocrPrintedText=true&imageDocClassification=false&align=false&ocrHandwrittenText=false";

    public String convertBase64ToText(String base64Content, User user) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("contentBase64", base64Content);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(OCR_API_URL, request, String.class);

            // Log the processing result
            ProcessingLog log = new ProcessingLog();
            log.setUser(user);
            log.setActionType("Convert Base64 to Text");
            log.setActionDate(LocalDateTime.now());
            log.setIsSuccess(true);
            log.setActionDetails(response.getBody());
            processingLogRepository.save(log);

            return response.getBody();
        } catch (Exception e) {
            // Log the error
            ProcessingLog log = new ProcessingLog();
            log.setUser(user);
            log.setActionType("Convert Base64 to Text");
            log.setActionDate(LocalDateTime.now());
            log.setIsSuccess(false);
            log.setErrorMessage(e.getMessage());
            processingLogRepository.save(log);

            throw new RuntimeException("Error calling OCR API: " + e.getMessage(), e);
        }
    }
}
