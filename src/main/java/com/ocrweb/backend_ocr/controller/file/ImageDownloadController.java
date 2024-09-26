package com.ocrweb.backend_ocr.controller.file;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/auth/converter")
public class ImageDownloadController {

    private static final Dotenv dotenv = Dotenv.load();

    @GetMapping("/download-image")
    public ResponseEntity<Resource> downloadImage(@RequestParam String imageName, @RequestParam String format) {
        try {
            // Xác định định dạng file
            String fileExtension = format.equalsIgnoreCase("jpg") ? "jpg" : "png";
            String contentType = format.equalsIgnoreCase("jpg") ? MediaType.IMAGE_JPEG_VALUE : MediaType.IMAGE_PNG_VALUE;

            // Lấy đường dẫn tới thư mục hình ảnh từ file .env
            String outputDirectory = dotenv.get("IMAGE_OUTPUT_DIRECTORY");

            // Đường dẫn tới file hình ảnh
            Path filePath = Paths.get(outputDirectory + imageName + "." + fileExtension);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
}
