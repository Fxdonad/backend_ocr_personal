package com.ocrweb.backend_ocr.service.pdf;

import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.file.FolderFiles;
import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.documents.DocumentRepository;
import com.ocrweb.backend_ocr.repository.file.UploadedFileRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderFilesRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderUploadsRepository;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private FolderUploadsRepository folderUploadsRepository;

    @Autowired
    private FolderFilesRepository folderFilesRepository;

    @Autowired
    private DocumentRepository documentRepository;

    private final String pdfDir = "D:\\Project\\Project_WebOCR\\file_uploads\\";

    // Create a new PDF
    public void createPdf(User user, String fileName) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Hello, This is a OCR Web Service!");
            contentStream.endText();
        }

        File file = new File(pdfDir, fileName);
        document.save(file);
        document.close();

        saveFileToDatabase(user, null, file.getName());
    }

    // Load an existing PDF
    public PDDocument loadPdf(String fileName) throws IOException {
        File file = new File(pdfDir, fileName);
        if (!file.exists()) {
            throw new IOException("File not found: " + fileName);
        }
        return PDDocument.load(file);
    }

    public void splitFile(User user, String fileName) throws IOException {
        String extension = getFileExtension(fileName);

        switch (extension) {
            case "pdf":
                splitPdf(user, fileName);
                break;
            case "jpg":
            case "png":
                splitImage(user, fileName);
                break;
            case "docx":
                splitDocx(user, fileName);
                break;
            case "xlsx":
                splitXlsx(user, fileName);
                break;
            case "pptx":
                splitPptx(user, fileName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private void splitPdf(User user, String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        List<PDPage> pages = new ArrayList<>();
        for (PDPage page : document.getPages()) {
            pages.add(page);
        }

        for (int i = 0; i < pages.size(); i++) {
            PDDocument singlePageDoc = new PDDocument();
            singlePageDoc.addPage(pages.get(i));
            String splitFileName = "split_" + i + "_" + fileName;
            singlePageDoc.save(new File(pdfDir, splitFileName));
            singlePageDoc.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        document.close();
    }


    private void splitImage(User user, String fileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(pdfDir, fileName));
        int halfWidth = image.getWidth() / 2;
        int halfHeight = image.getHeight() / 2;

        BufferedImage topLeft = image.getSubimage(0, 0, halfWidth, halfHeight);
        BufferedImage topRight = image.getSubimage(halfWidth, 0, halfWidth, halfHeight);
        BufferedImage bottomLeft = image.getSubimage(0, halfHeight, halfWidth, halfHeight);
        BufferedImage bottomRight = image.getSubimage(halfWidth, halfHeight, halfWidth, halfHeight);

        String[] splitFileNames = {
                "split_topLeft_" + fileName,
                "split_topRight_" + fileName,
                "split_bottomLeft_" + fileName,
                "split_bottomRight_" + fileName
        };

        ImageIO.write(topLeft, getFileExtension(fileName), new File(pdfDir, splitFileNames[0]));
        ImageIO.write(topRight, getFileExtension(fileName), new File(pdfDir, splitFileNames[1]));
        ImageIO.write(bottomLeft, getFileExtension(fileName), new File(pdfDir, splitFileNames[2]));
        ImageIO.write(bottomRight, getFileExtension(fileName), new File(pdfDir, splitFileNames[3]));

        // Lưu các tệp đã split vào cơ sở dữ liệu
        for (String splitFileName : splitFileNames) {
            saveFileToDatabase(user, fileName, splitFileName);
        }
    }

    private void splitDocx(User user, String fileName) throws IOException {
        XWPFDocument document = new XWPFDocument(new FileInputStream(pdfDir + fileName));
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFDocument newDoc = new XWPFDocument();
            newDoc.createParagraph().createRun().setText(paragraphs.get(i).getText());

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newDoc.write(out);
            }
            newDoc.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        document.close();
    }


    private void splitXlsx(User user, String fileName) throws IOException {
        XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(pdfDir + fileName));
        int numberOfSheets = workbook.getNumberOfSheets();

        for (int i = 0; i < numberOfSheets; i++) {
            XSSFWorkbook newWorkbook = new XSSFWorkbook();
            newWorkbook.createSheet(workbook.getSheetName(i));

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newWorkbook.write(out);
            }
            newWorkbook.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        workbook.close();
    }

    private void splitPptx(User user, String fileName) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(pdfDir + fileName));
        List<XSLFSlide> slides = ppt.getSlides();

        for (int i = 0; i < slides.size(); i++) {
            XMLSlideShow newPpt = new XMLSlideShow();
            newPpt.createSlide().importContent(slides.get(i));

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newPpt.write(out);
            }
            newPpt.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        ppt.close();
    }

    public void splitFileByRange(User user, String fileName, int start, int end) throws IOException {
        String extension = getFileExtension(fileName);

        switch (extension) {
            case "pdf":
                splitPdfByRange(user, fileName, start, end);
                break;
            case "jpg":
            case "png":
                splitImageByRange(user, fileName, start, end);
                break;
            case "docx":
                splitDocxByRange(user, fileName, start, end);
                break;
            case "xlsx":
                splitXlsxByRange(user, fileName, start, end);
                break;
            case "pptx":
                splitPptxByRange(user, fileName, start, end);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    private void splitPdfByRange(User user, String fileName, int startPage, int endPage) throws IOException {
        PDDocument document = loadPdf(fileName);
        int totalPages = document.getNumberOfPages();

        if (startPage < 1 || endPage > totalPages || startPage > endPage) {
            document.close();
            throw new IllegalArgumentException("Invalid start or end page number.");
        }

        for (int i = startPage - 1; i < endPage; i++) {
            PDDocument singlePageDoc = new PDDocument();
            singlePageDoc.addPage(document.getPage(i));
            String splitFileName = "split_" + (i + 1) + "_" + fileName;
            singlePageDoc.save(new File(pdfDir, splitFileName));
            singlePageDoc.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        document.close();
    }

    private void splitImageByRange(User user, String fileName, int startX, int endX) throws IOException {
        BufferedImage image = ImageIO.read(new File(pdfDir, fileName));
        int width = image.getWidth();

        if (startX < 0 || endX > width || startX >= endX) {
            throw new IllegalArgumentException("Invalid start or end position.");
        }

        BufferedImage subImage = image.getSubimage(startX, 0, endX - startX, image.getHeight());
        String splitFileName = "split_range_" + startX + "_" + endX + "_" + fileName;
        ImageIO.write(subImage, getFileExtension(fileName), new File(pdfDir, splitFileName));

        // Lưu tệp đã split vào cơ sở dữ liệu
        saveFileToDatabase(user, fileName, splitFileName);
    }

    private void splitDocxByRange(User user, String fileName, int startParagraph, int endParagraph) throws IOException {
        XWPFDocument document = new XWPFDocument(new FileInputStream(pdfDir + fileName));
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        if (startParagraph < 1 || endParagraph > paragraphs.size() || startParagraph > endParagraph) {
            document.close();
            throw new IllegalArgumentException("Invalid start or end paragraph number.");
        }

        for (int i = startParagraph - 1; i < endParagraph; i++) {
            XWPFDocument newDoc = new XWPFDocument();
            newDoc.createParagraph().createRun().setText(paragraphs.get(i).getText());

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newDoc.write(out);
            }
            newDoc.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        document.close();
    }

    private void splitXlsxByRange(User user, String fileName, int startSheet, int endSheet) throws IOException {
        XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(new FileInputStream(pdfDir + fileName));
        int totalSheets = workbook.getNumberOfSheets();

        if (startSheet < 1 || endSheet > totalSheets || startSheet > endSheet) {
            workbook.close();
            throw new IllegalArgumentException("Invalid start or end sheet number.");
        }

        for (int i = startSheet - 1; i < endSheet; i++) {
            XSSFWorkbook newWorkbook = new XSSFWorkbook();
            newWorkbook.createSheet(workbook.getSheetName(i));

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newWorkbook.write(out);
            }
            newWorkbook.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        workbook.close();
    }

    private void splitPptxByRange(User user, String fileName, int startSlide, int endSlide) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(pdfDir + fileName));
        List<XSLFSlide> slides = ppt.getSlides();

        if (startSlide < 1 || endSlide > slides.size() || startSlide > endSlide) {
            ppt.close();
            throw new IllegalArgumentException("Invalid start or end slide number.");
        }

        for (int i = startSlide - 1; i < endSlide; i++) {
            XMLSlideShow newPpt = new XMLSlideShow();
            newPpt.createSlide().importContent(slides.get(i));

            String splitFileName = "split_" + i + "_" + fileName;
            try (FileOutputStream out = new FileOutputStream(new File(pdfDir, splitFileName))) {
                newPpt.write(out);
            }
            newPpt.close();

            // Lưu tệp đã split vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, splitFileName);
        }

        ppt.close();
    }

    // Extract PDF Page
    public void extractPage(User user, String fileName, int pageNumber) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDDocument newDoc = new PDDocument();
        newDoc.addPage(document.getPage(pageNumber - 1));
        String newFileName = "extractedPage_" + pageNumber + "_" + fileName;
        newDoc.save(new File(pdfDir, newFileName));
        newDoc.close();
        document.close();

        saveFileToDatabase(user, fileName, newFileName);
    }

    // Merge PDF to New File
    public void mergePdfs(User user, List<String> fileNames, String outputFileName) throws IOException {
        PDFMergerUtility mergerUtility = new PDFMergerUtility();
        mergerUtility.setDestinationFileName(new File(pdfDir, outputFileName).getAbsolutePath());

        for (String fileName : fileNames) {
            mergerUtility.addSource(new File(pdfDir, fileName));
        }

        mergerUtility.mergeDocuments(null);

        // Save the merged file to the database
        saveFileToDatabase(user, null, outputFileName);
    }


    // Get PDF properties
    public String getDocumentProperties(String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDDocumentInformation info = document.getDocumentInformation();

        StringBuilder properties = new StringBuilder();
        properties.append("Title: ").append(info.getTitle()).append("\n");
        properties.append("Author: ").append(info.getAuthor()).append("\n");
        properties.append("Subject: ").append(info.getSubject()).append("\n");
        properties.append("Keywords: ").append(info.getKeywords()).append("\n");
        properties.append("Creator: ").append(info.getCreator()).append("\n");
        properties.append("Producer: ").append(info.getProducer()).append("\n");
        properties.append("Creation Date: ").append(info.getCreationDate()).append("\n");
        properties.append("Modification Date: ").append(info.getModificationDate()).append("\n");

        document.close();

        return properties.toString();
    }

    public void updateDocumentProperties(User user, String fileName, String title, String author, String subject, String keywords) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDDocumentInformation info = document.getDocumentInformation();

        info.setTitle(title);
        info.setAuthor(author);
        info.setSubject(subject);
        info.setKeywords(keywords);

        document.save(new File(pdfDir, fileName));
        document.close();

        // Update the file in the database
        saveFileToDatabase(user, null, fileName);
    }


    // Set a password on a PDF
    public void setPdfPassword(User user, String fileName, String password) throws IOException {
        PDDocument document = loadPdf(fileName);
        AccessPermission accessPermission = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, accessPermission);
        spp.setEncryptionKeyLength(128);
        spp.setPermissions(accessPermission);
        document.protect(spp);

        String newFileName = "protected_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the protected file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    // Add text to a PDF
    public void addTextToPdf(User user, String fileName, String text, int x, int y) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();

        String newFileName = "textAdded_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the modified file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    public void addImageToPdf(User user, String fileName, String imagePath, int x, int y, int width, int height) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDPage page = document.getPage(0);
        PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.drawImage(pdImage, x, y, width, height);
        contentStream.close();

        String newFileName = "imageAdded_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the modified file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    public void setFontAndSize(User user, String fileName, String text, String fontName, int fontSize, int x, int y) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDPage page = document.getPage(0);

        PDType1Font font = PDType1Font.HELVETICA; // Default font
        switch (fontName.toUpperCase()) {
            case "COURIER":
                font = PDType1Font.COURIER;
                break;
            case "TIMES_ROMAN":
                font = PDType1Font.TIMES_ROMAN;
                break;
            case "HELVETICA_BOLD":
                font = PDType1Font.HELVETICA_BOLD;
                break;
            default:
                break;
        }

        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();

        String newFileName = "fontUpdated_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the updated file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    // Convert PDF to images
    public void convertPdfToImage(User user, String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
            String imageFileName = "image_" + (i + 1) + "_" + fileName.replace(".pdf", ".jpg");
            ImageIOUtil.writeImage(bim, pdfDir + imageFileName, 300);

            // Lưu từng tệp hình ảnh vào cơ sở dữ liệu
            saveFileToDatabase(user, fileName, imageFileName);
        }
        document.close();
    }


    // Convert PDF to Word (DOCX)
    public void convertPdfToDocx(User user, String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDFTextStripper pdfStripper = new PDFTextStripper();

        String newFileName = fileName.replace(".pdf", ".docx");
        File docxFile = new File(pdfDir, newFileName);
        try (XWPFDocument docxDocument = new XWPFDocument(); FileOutputStream out = new FileOutputStream(docxFile)) {
            String[] lines = pdfStripper.getText(document).split("\n");
            for (String line : lines) {
                docxDocument.createParagraph().createRun().setText(line);
            }
            docxDocument.write(out);
        }
        document.close();

        // Save the DOCX file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }


    // Convert PDF to Excel (XLSX)
    public void convertPdfToXlsx(User user, String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDFTextStripper pdfStripper = new PDFTextStripper();

        String newFileName = fileName.replace(".pdf", ".xlsx");
        File xlsxFile = new File(pdfDir, newFileName);
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(xlsxFile)) {
            Sheet sheet = workbook.createSheet("PDF Data");
            String[] lines = pdfStripper.getText(document).split("\n");
            int rowNum = 0;
            for (String line : lines) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(line);
            }
            workbook.write(out);
        }
        document.close();

        // Save the XLSX file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }


    public void convertPdfToPptx(User user, String fileName) throws IOException {
        PDDocument document = loadPdf(fileName);
        PDFTextStripper pdfStripper = new PDFTextStripper();

        String newFileName = fileName.replace(".pdf", ".pptx");
        File pptxFile = new File(pdfDir, newFileName);
        try (XMLSlideShow ppt = new XMLSlideShow(); FileOutputStream out = new FileOutputStream(pptxFile)) {
            String[] lines = pdfStripper.getText(document).split("\n");
            for (String line : lines) {
                XSLFSlide slide = ppt.createSlide();
                slide.createTextBox().setText(line);
            }
            ppt.write(out);
        }
        document.close();

        // Save the PPTX file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    public void deletePagesByRange(User user, String fileName, int startPage, int endPage) throws IOException {
        PDDocument document = loadPdf(fileName);
        for (int i = endPage - 1; i >= startPage - 1; i--) {
            document.removePage(i);
        }
        String newFileName = "deletedPages_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the new file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    // Set page size of a PDF
    public void setPageSize(User user, String fileName, PDRectangle pageSize) throws IOException {
        PDDocument document = loadPdf(fileName);
        int pageCount = document.getNumberOfPages();

        for (int i = 0; i < pageCount; i++) {
            PDPage page = document.getPage(i);
            page.setMediaBox(pageSize);
        }

        String newFileName = "resized_" + fileName;
        document.save(new File(pdfDir, newFileName));
        document.close();

        // Save the resized file to the database
        saveFileToDatabase(user, fileName, newFileName);
    }

    // Save file and document to the database
    private void saveFileToDatabase(User user, String originalFileName, String newFileName) throws IOException {
        UploadedFiles uploadedFile = new UploadedFiles();
        uploadedFile.setFileName(newFileName);
        uploadedFile.setFilePath(new File(pdfDir, newFileName).getAbsolutePath());
        uploadedFile.setFileSize(Files.size(Paths.get(pdfDir, newFileName)));
        uploadedFile.setFileType("PDF");
        uploadedFile.setUploadDate(LocalDateTime.now());
        uploadedFile.setUser(user);

        uploadedFile = uploadedFileRepository.save(uploadedFile);

        // Create and save the Document entity
        Document document = new Document();
        document.setUser(user);
        document.setFile(uploadedFile);
        document.setDocumentName(newFileName);
        document.setDocumentType("PDF");
        document.setUploadDate(LocalDateTime.now());
        document.setFilePath(uploadedFile.getFilePath());
        document.setStatus("Active");
        documentRepository.save(document);
    }


    // Hàm hỗ trợ để chuyển đổi tên kích thước thành PDRectangle
    private PDRectangle getPageSizeFromName(String pageSizeName) {
        switch (pageSizeName.toUpperCase()) {
            case "A4":
                return PDRectangle.A4;
            case "LETTER":
                return PDRectangle.LETTER;
            case "LEGAL":
                return PDRectangle.LEGAL;
            // Thêm các kích thước khác nếu cần
            default:
                return null;
        }
    }
}
