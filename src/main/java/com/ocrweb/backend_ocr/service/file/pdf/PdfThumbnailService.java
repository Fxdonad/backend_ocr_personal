package com.ocrweb.backend_ocr.service.file.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class PdfThumbnailService {

    public String createPdfThumbnail(MultipartFile file) {
        PDDocument document = null;
        try {
            // Đọc tài liệu PDF
            document = PDDocument.load(file.getInputStream());

            // Sử dụng PDFRenderer để render trang đầu tiên thành hình ảnh
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

            // Chuyển đổi BufferedImage thành Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            // Trả về hình ảnh dưới dạng chuỗi Base64
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Đảm bảo giải phóng tài liệu PDF để tránh rò rỉ bộ nhớ
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
