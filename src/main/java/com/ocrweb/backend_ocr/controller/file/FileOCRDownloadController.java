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
public class FileOCRDownloadController {

    private static final Dotenv dotenv = Dotenv.load();

    @GetMapping("/download-pdf")
    public ResponseEntity<Resource> downloadPDF(@RequestParam String fileName) {
        try {
            // Lấy đường dẫn tới thư mục PDF từ file .env
            String outputDirectory = dotenv.get("PDF_OUTPUT_DIRECTORY");

            // Đường dẫn tới file PDF
            Path filePath = Paths.get(outputDirectory + fileName + ".pdf");
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
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
