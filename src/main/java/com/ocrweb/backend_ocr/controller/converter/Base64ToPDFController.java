package com.ocrweb.backend_ocr.controller.converter;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.converter.Base64ToPDFService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/converter")
public class Base64ToPDFController {

    @Autowired
    private Base64ToPDFService base64ToPDFService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/base64-to-pdf")
    public ResponseEntity<?> convertBase64ToPdf(@RequestHeader("Authorization") String token,
                                                @RequestBody Map<String, String> payload) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            String base64String = payload.get("base64String");
            String fileName = payload.get("fileName");

            if (base64String == null || fileName == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Base64 string or file name is missing.");
            }

            File pdfFile = base64ToPDFService.convertBase64ToPdf(base64String, fileName, user);

            if (pdfFile == null || !pdfFile.exists()) {
                throw new RuntimeException("Failed to create PDF file.");
            }

            // Chuẩn bị file để trả về
            Path filePath = pdfFile.toPath();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File not found or not readable.");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IO Exception occurred: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
