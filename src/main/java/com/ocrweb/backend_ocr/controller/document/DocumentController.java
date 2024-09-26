//package com.ocrweb.backend_ocr.controller.document;
//
//import com.ocrweb.backend_ocr.controller.converter.OCRController;
//import com.ocrweb.backend_ocr.entity.documents.Document;
//import com.ocrweb.backend_ocr.entity.logs.ActionLogRequest;
//import com.ocrweb.backend_ocr.entity.requests.DocumentRequest;
//import com.ocrweb.backend_ocr.entity.user.User;
//import com.ocrweb.backend_ocr.service.actions.UserActionService;
//import com.ocrweb.backend_ocr.service.converter.Base64ToPDFService;
//import com.ocrweb.backend_ocr.service.documents.DocumentService;
//import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
//import com.ocrweb.backend_ocr.service.pdf.PdfService;
//import com.ocrweb.backend_ocr.service.user.UserService;
//import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/document")
//public class DocumentController {
//
//    @Autowired
//    private DocumentService documentService;
//
//    @Autowired
//    private Base64ToPDFService base64ToPDFService;
//
//    @Autowired
//    private PdfService pdfService;
//
//    @Autowired
//    private ProcessingLogService processingLogService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    UserActionService userActionService;
//
//    @PostMapping("/processAndSplit")
//    public ResponseEntity<?> processAndSplitDocument(@RequestHeader("Authorization") String token,
//                                                     @RequestBody ActionLogRequest request) {
//        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
//        User user = userService.findByUsername(username);
//        Document document = documentService.getDocumentByIdAndUser(request.getDocumentId(), user);
//
//        String documentType = document.getDocumentType();
//        boolean success = false;
//        List<String> splitFilePaths = new ArrayList<>();
//
//        try {
//            if ("JPG".equalsIgnoreCase(documentType) || "PNG".equalsIgnoreCase(documentType)) {
//                // Tạo thư mục đầu ra cho file đã split
//                String outputDir = "E:\\Learn\\ocr_service\\output_pdf\\split_" + System.currentTimeMillis();
//
//                // Chuyển đổi hình ảnh từ Base64 sang PDF và tách PDF
//                splitFilePaths = pdfService.convertImageToPdfAndSplit(document.getBase64(), document.getDocumentName().replaceAll("\\.[^.]*$", "") + ".pdf", outputDir);
//
//                document.setDocumentType("PDF");
//                documentService.save(document);
//            } else if ("PDF".equalsIgnoreCase(documentType)) {
//                // Tách PDF nếu đó là file PDF
//                splitFilePaths = pdfService.splitPdfFilePathInDirectory(document.getFilePath(), "E:\\Learn\\ocr_service\\output_pdf\\split_" + System.currentTimeMillis());
//            }
//
//            success = true;
//            return ResponseEntity.ok(splitFilePaths);
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("Error processing and splitting document: " + e.getMessage());
//        } finally {
//            // Ghi log hành động và kết quả xử lý
//            userActionService.logUserAction(user, document, request.getActionType(), success);
//            processingLogService.logProcessing(document.getFile(), user, "ProcessAndSplitDocument", success, success ? null : "Failed to process and split document");
//        }
//    }
//
//    @PostMapping("/split")
//    public ResponseEntity<String> splitPdf(@RequestHeader("Authorization") String token,
//                                           @RequestBody DocumentRequest request) {
//        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
//        User user = userService.findByUsername(username);
//        Document document = documentService.getDocumentByIdAndUser(request.getDocumentId(), user);
//
//        if (!"PDF".equalsIgnoreCase(document.getDocumentType())) {
//            return ResponseEntity.badRequest().body("Document is not a PDF.");
//        }
//
//        String filePath = document.getFilePath();
//        File file = new File(document.getFilePath());
//
//        System.out.println("File path: " + filePath);
//        System.out.println("File exists: " + file.exists());
//
//        if (!file.exists()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + document.getFilePath());
//        }
//        if (!file.canRead()) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot read file: " + document.getFilePath());
//        }
//
//        try {
//            pdfService.splitPdfFilePath(document.getFilePath());
//            userActionService.logUserAction(user, document, "Split PDF", true);
//            processingLogService.logProcessing(document.getFile(), user, "Split PDF", true, null);
//            return ResponseEntity.ok("PDF split successfully.");
//        } catch (IOException e) {
//            String errorMessage = "Error splitting PDF: " + e.getMessage() + " | FilePath: " + filePath;
//            userActionService.logUserAction(user, document, "Split PDF", false);
//            processingLogService.logProcessing(document.getFile(), user, "Split PDF", false, e.getMessage());
//            return ResponseEntity.status(500).body("Error splitting PDF: " + e.getMessage());
//        }
//    }
//}
//
