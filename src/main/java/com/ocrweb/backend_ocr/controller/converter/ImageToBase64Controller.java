package com.ocrweb.backend_ocr.controller.converter;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.converter.ImageToBase64Service;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/converter")
public class ImageToBase64Controller {

    @Autowired
    private ImageToBase64Service imageToBase64Service;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/image-to-base64")
    public ResponseEntity<String> convertImageToBase64(@RequestHeader("Authorization") String token,
                                                       @RequestParam("file") MultipartFile file) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        try {
            // Convert image to Base64 and log the process
            String base64String = imageToBase64Service.convertImageToBase64(file, user);

            return ResponseEntity.ok(base64String);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Có lỗi xảy ra trong quá trình xử lý ảnh: " + e.getMessage());
        }
    }
}
