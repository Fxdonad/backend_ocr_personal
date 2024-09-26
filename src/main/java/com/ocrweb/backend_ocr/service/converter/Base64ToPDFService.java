package com.ocrweb.backend_ocr.service.converter;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;


@Service
public class Base64ToPDFService {

    @Autowired
    private ProcessingLogService processingLogService;

    // Đường dẫn đến thư mục tessdata của Tesseract
    @Value("${TESSDATA_PATH}")
    private String tessdataPath;

    // Đường dẫn đến file font để thêm vào PDF
    @Value("${FONT_PATH}")
    private String fontPath;

    // Đường dẫn thư mục lưu trữ file PDF đầu ra
    @Value("${PDF_OUTPUT_DIRECTORY}")
    private String pdfOutputDirectory;

    /**
     * Chuyển đổi chuỗi Base64 thành file PDF sử dụng Tesseract OCR.
     *
     * @param base64String Chuỗi Base64 của hình ảnh.
     * @param fileName     Tên file PDF đầu ra (không bao gồm phần mở rộng).
     * @param user         Người dùng thực hiện hành động.
     * @return File PDF được tạo ra.
     * @throws IOException Nếu có lỗi trong quá trình xử lý.
     */
    public File convertBase64ToPdf(String base64String, String fileName, User user) throws IOException {
        File outputFile = null;
        boolean success = false;
        try {
            // Bước 1: Giải mã chuỗi Base64 thành BufferedImage
            BufferedImage image = decodeBase64ToImage(base64String);
            if (image == null) {
                throw new IOException("Không thể giải mã chuỗi Base64 thành hình ảnh.");
            }

            // Bước 2: Thực hiện OCR bằng Tesseract để trích xuất văn bản
            String extractedText = performOCR(image);

            // Bước 3: Tạo PDF với hình ảnh và văn bản đã trích xuất
            outputFile = createSearchablePdf(image, extractedText, fileName);

            success = true;
        } catch (TesseractException e) {
            processingLogService.logProcessing(null, user, "ConvertBase64ToPDF", false, e.getMessage());
            throw new IOException("Lỗi OCR của Tesseract: " + e.getMessage(), e);
        } catch (IOException e) {
            processingLogService.logProcessing(null, user, "ConvertBase64ToPDF", false, e.getMessage());
            throw e;
        } catch (Exception e) {
            processingLogService.logProcessing(null, user, "ConvertBase64ToPDF", false, e.getMessage());
            throw new IOException("Lỗi không mong muốn: " + e.getMessage(), e);
        } finally {
            processingLogService.logProcessing(null, user, "ConvertBase64ToPDF", success, null);
        }
        return outputFile;
    }

    /**
     * Giải mã chuỗi Base64 thành BufferedImage.
     *
     * @param base64String Chuỗi Base64 của hình ảnh.
     * @return BufferedImage hoặc null nếu không thể giải mã.
     * @throws IOException Nếu có lỗi trong quá trình đọc hình ảnh.
     */
    private BufferedImage decodeBase64ToImage(String base64String) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(bis);
        bis.close();
        return image;
    }

    /**
     * Thực hiện OCR trên BufferedImage sử dụng Tesseract.
     *
     * @param image BufferedImage của hình ảnh cần OCR.
     * @return Văn bản đã trích xuất từ hình ảnh.
     * @throws TesseractException Nếu có lỗi trong quá trình OCR.
     */
    private String performOCR(BufferedImage image) throws TesseractException {
        ITesseract tesseract = new Tesseract();
        // Set TESSDATA_PREFIX explicitly
        System.setProperty("TESSDATA_PREFIX", tessdataPath);
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng"); // Thiết lập ngôn ngữ theo tessdata bạn đã tải

        // Thực hiện OCR và trích xuất văn bản
        return tesseract.doOCR(image);
    }


    /**
     * Tạo file PDF có chứa hình ảnh và lớp văn bản đã trích xuất.
     *
     * @param image        Hình ảnh gốc.
     * @param extractedText Văn bản đã trích xuất từ hình ảnh.
     * @param fileName      Tên file PDF đầu ra (không bao gồm phần mở rộng).
     * @return File PDF được tạo ra.
     * @throws IOException Nếu có lỗi trong quá trình tạo PDF.
     */
    private File createSearchablePdf(BufferedImage image, String extractedText, String fileName) throws IOException {
        PDDocument document = null;
        PDPageContentStream contentStream = null;
        File outputFile = null;

        try {
            document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
            document.addPage(page);

            // Tải font
            File fontFile = new File(fontPath);
            if (!fontFile.exists() || !fontFile.isFile()) {
                throw new IOException("Không tìm thấy file font tại đường dẫn: " + fontPath);
            }
            PDType0Font font = PDType0Font.load(document, fontFile);

            // Tạo đối tượng hình ảnh từ BufferedImage
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            // Vẽ hình ảnh lên trang PDF
            contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());

            // Thêm văn bản đã trích xuất làm lớp văn bản (text layer)
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.setNonStrokingColor(255, 255, 255); // Đặt màu trắng để làm cho văn bản vô hình
            contentStream.newLineAtOffset(25, image.getHeight() - 25); // Điều chỉnh vị trí bắt đầu

            String[] lines = extractedText.split("\n");
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLine();
            }

            contentStream.endText();
            contentStream.close();

            // Lưu PDF
            outputFile = new File(pdfOutputDirectory, fileName + ".pdf");
            // Đảm bảo thư mục đầu ra tồn tại
            File outputDir = new File(pdfOutputDirectory);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            document.save(outputFile);
        } catch (IOException e) {
            throw new IOException("Lỗi tạo PDF: " + e.getMessage(), e);
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    System.err.println("Lỗi đóng content stream: " + e.getMessage());
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    System.err.println("Lỗi đóng tài liệu PDF: " + e.getMessage());
                }
            }
        }

        return outputFile;
    }
}
