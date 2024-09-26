package com.ocrweb.backend_ocr.service.converter;

import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class ImageToBase64Service {

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    // Phương thức mới để chuyển đổi BufferedImage sang Base64
    public String convertBufferedImageToBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String convertImageToBase64Upload(MultipartFile file) throws IOException {
        // Đọc file ảnh từ MultipartFile
        BufferedImage image = ImageIO.read(file.getInputStream());

        // Chuyển đổi BufferedImage sang byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Chuyển đổi byte array sang Base64
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    public String convertImageToBase64(MultipartFile file, User user) throws IOException {
        String base64String = null;
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream); // Assume image is jpg
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64String = Base64.getEncoder().encodeToString(imageBytes);

            // Ghi log vào ProcessingLogs
            ProcessingLog log = new ProcessingLog();
            log.setUser(user);
            log.setActionType("Convert Image to Base64");
            log.setActionDate(LocalDateTime.now());
            log.setIsSuccess(true);
            processingLogRepository.save(log);

        } catch (Exception e) {
            // Ghi log lỗi vào ProcessingLogs
            ProcessingLog log = new ProcessingLog();
            log.setUser(user);
            log.setActionType("Convert Image to Base64");
            log.setActionDate(LocalDateTime.now());
            log.setIsSuccess(false);
            log.setErrorMessage(e.getMessage());
            processingLogRepository.save(log);

            throw new IOException("Error converting image to Base64", e);
        }

        return base64String;
    }
}
