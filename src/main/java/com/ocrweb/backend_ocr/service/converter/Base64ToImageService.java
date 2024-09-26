package com.ocrweb.backend_ocr.service.converter;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import com.ocrweb.backend_ocr.util.ocrprocess.Base64ToImageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class Base64ToImageService {

    @Autowired
    private ProcessingLogService processingLogService;

    public File convertBase64ToImage(String base64String, String format, String fileName, User user) throws IOException {
        File outputFile;
        boolean success = false;
        try {
            outputFile = Base64ToImageConverter.convertBase64ToImage(base64String, format, fileName);
            success = true;
        } catch (Exception e) {
            processingLogService.logProcessing(null, user, "ConvertBase64ToImage", false, e.getMessage());
            throw e;
        } finally {
            processingLogService.logProcessing(null, user, "ConvertBase64ToImage", success, null);
        }
        return outputFile;
    }
}

