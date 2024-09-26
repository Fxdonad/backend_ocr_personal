package com.ocrweb.backend_ocr.util.ocrprocess;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import java.io.File;
import java.io.IOException;
import io.github.cdimascio.dotenv.Dotenv;

public class TextToPDFConverter {

    private static final Dotenv dotenv = Dotenv.load();

    public static File convertTextToPDF(String text, String outputFileName) throws IOException {
        PDDocument doc = null;
        PDPageContentStream contentStream = null;
        File outputFile = null;

        try {
            doc = new PDDocument();
            PDPage page = new PDPage();
            doc.addPage(page);

            // Đọc đường dẫn font từ file .env
            String fontPath = dotenv.get("FONT_PATH");
            File fontFile = new File(fontPath);

            if (!fontFile.exists() || !fontFile.isFile()) {
                throw new IOException("Font file not found at path: " + fontPath);
            }

            PDType0Font font = PDType0Font.load(doc, fontFile);

            contentStream = new PDPageContentStream(doc, page);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.setLeading(14.5f);  // Khoảng cách giữa các dòng
            contentStream.newLineAtOffset(25, 750);

            // Chia văn bản thành từng dòng
            String[] lines = text.split("\n");
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLine();
            }

            contentStream.endText();
            contentStream.close();

            // Đọc đường dẫn lưu file PDF từ file .env
            String outputDirectory = dotenv.get("PDF_OUTPUT_DIRECTORY");
            outputFile = new File(outputDirectory + outputFileName + ".pdf");
            doc.save(outputFile);
        } catch (IOException e) {
            throw new IOException("Error saving PDF: " + e.getMessage(), e);
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing content stream: " + e.getMessage());
                }
            }
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    System.err.println("Error closing document: " + e.getMessage());
                }
            }
        }

        return outputFile;
    }
}
