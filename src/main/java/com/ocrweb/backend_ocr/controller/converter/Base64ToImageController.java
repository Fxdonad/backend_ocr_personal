package com.ocrweb.backend_ocr.controller.converter;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.converter.Base64ToImageService;
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
public class Base64ToImageController {

    @Autowired
    private Base64ToImageService base64ToImageService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/base64-to-image")
    public ResponseEntity<Resource> convertBase64ToImage(@RequestHeader("Authorization") String token,
                                                         @RequestBody Map<String, String> payload) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        String base64String = payload.get("base64String");
        String format = payload.get("format");
        String fileName = payload.get("fileName");

        File imageFile = base64ToImageService.convertBase64ToImage(base64String, format, fileName, user);

        // Chuẩn bị file để trả về
        Path filePath = imageFile.toPath();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/" + format))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("Failed to download the file");
        }
    }
}
