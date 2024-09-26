package com.ocrweb.backend_ocr.util.ocrprocess;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;
import io.github.cdimascio.dotenv.Dotenv;

public class ImageToTextExtractor {

    private static final Dotenv dotenv = Dotenv.load();

    public static String extractTextFromImage(BufferedImage image) {
        ITesseract tesseract = new Tesseract();

        // Lấy đường dẫn từ file .env
        String tessdataPath = dotenv.get("TESSDATA_PATH");
        tesseract.setDatapath(tessdataPath); // Đường dẫn đến thư mục chứa dữ liệu Tesseract

        // Đặt ngôn ngữ cho Tesseract (Ví dụ: "vie" cho tiếng Việt)
        String language = dotenv.get("TESSDATA_LANGUAGE", "vie"); // Mặc định là tiếng Anh nếu không có cấu hình
        tesseract.setLanguage(language);

        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
            return null;
        }
    }
}
