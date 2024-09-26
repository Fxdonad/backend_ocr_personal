package com.ocrweb.backend_ocr.controller.pdf ;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.pdf.PdfService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<?> createPdf(@RequestHeader("Authorization") String token,
                                       @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.createPdf(user, fileName);
            return ResponseEntity.ok("PDF created successfully with filename: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error creating PDF: " + e.getMessage());
        }
    }

    @GetMapping("/load")
    public ResponseEntity<?> loadPdf(@RequestHeader("Authorization") String token,
                                     @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.loadPdf(fileName);
            return ResponseEntity.ok("PDF loaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error loading PDF: " + e.getMessage());
        }
    }

    @PostMapping("/split")
    public ResponseEntity<?> splitFile(@RequestHeader("Authorization") String token,
                                       @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.splitFile(user, fileName);
            return ResponseEntity.ok("File split successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error splitting file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/splitByRange")
    public ResponseEntity<?> splitFileByRange(@RequestHeader("Authorization") String token,
                                              @RequestParam String fileName,
                                              @RequestParam int start,
                                              @RequestParam int end) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.splitFileByRange(user, fileName, start, end);
            return ResponseEntity.ok("File split by range successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error splitting file by range: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/deletePagesByRange")
    public ResponseEntity<?> deletePagesByRange(@RequestHeader("Authorization") String token,
                                                @RequestParam String fileName,
                                                @RequestParam int startPage,
                                                @RequestParam int endPage) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.deletePagesByRange(user, fileName, startPage, endPage);
            return ResponseEntity.ok("Pages deleted by range successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error deleting pages by range: " + e.getMessage());
        }
    }

    @PostMapping("/extractPage")
    public ResponseEntity<?> extractPage(@RequestHeader("Authorization") String token,
                                         @RequestParam String fileName,
                                         @RequestParam int pageNumber) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.extractPage(user, fileName, pageNumber);
            return ResponseEntity.ok("Page extracted successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error extracting page: " + e.getMessage());
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<?> mergePdfs(@RequestHeader("Authorization") String token,
                                       @RequestParam List<String> fileNames,
                                       @RequestParam String outputFileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.mergePdfs(user, fileNames, outputFileName);
            return ResponseEntity.ok("PDFs merged successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error merging PDFs: " + e.getMessage());
        }
    }

    @GetMapping("/properties")
    public ResponseEntity<?> getDocumentProperties(@RequestHeader("Authorization") String token,
                                                   @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            String properties = pdfService.getDocumentProperties(fileName);
            return ResponseEntity.ok(properties);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error retrieving PDF properties: " + e.getMessage());
        }
    }

    @PostMapping("/updateProperties")
    public ResponseEntity<?> updateDocumentProperties(@RequestHeader("Authorization") String token,
                                                      @RequestParam String fileName,
                                                      @RequestParam String title,
                                                      @RequestParam String author,
                                                      @RequestParam String subject,
                                                      @RequestParam String keywords) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.updateDocumentProperties(user, fileName, title, author, subject, keywords);
            return ResponseEntity.ok("Document properties updated successfully for: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error updating document properties: " + e.getMessage());
        }
    }

    @PostMapping("/encrypt")
    public ResponseEntity<?> encryptPdf(@RequestHeader("Authorization") String token,
                                        @RequestParam String fileName,
                                        @RequestParam String password) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.setPdfPassword(user, fileName, password);
            return ResponseEntity.ok("PDF encrypted successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error encrypting PDF: " + e.getMessage());
        }
    }

    @PostMapping("/addText")
    public ResponseEntity<?> addTextToPdf(@RequestHeader("Authorization") String token,
                                          @RequestParam String fileName,
                                          @RequestParam String text, @RequestParam int x,
                                          @RequestParam int y) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.addTextToPdf(user, fileName, text, x, y);
            return ResponseEntity.ok("Text added to PDF successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error adding text to PDF: " + e.getMessage());
        }
    }

    @PostMapping("/addImage")
    public ResponseEntity<?> addImageToPdf(@RequestHeader("Authorization") String token,
                                           @RequestParam String fileName,
                                           @RequestParam String imagePath,
                                           @RequestParam int x,
                                           @RequestParam int y,
                                           @RequestParam int width,
                                           @RequestParam int height) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.addImageToPdf(user, fileName, imagePath, x, y, width, height);
            return ResponseEntity.ok("Image added to PDF successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error adding image to PDF: " + e.getMessage());
        }
    }

    @PostMapping("/setFontAndSize")
    public ResponseEntity<?> setFontAndSize(@RequestHeader("Authorization") String token,
                                            @RequestParam String fileName,
                                            @RequestParam String text,
                                            @RequestParam String fontName,
                                            @RequestParam int fontSize,
                                            @RequestParam int x,
                                            @RequestParam int y) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.setFontAndSize(user, fileName, text, fontName, fontSize, x, y);
            return ResponseEntity.ok("Font and size set successfully in: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error setting font and size: " + e.getMessage());
        }
    }

    // Endpoint để set kích thước trang của một PDF
    @PostMapping("/setPageSize")
    public ResponseEntity<?> setPageSize(@RequestHeader("Authorization") String token,
                                         @RequestParam String fileName,
                                         @RequestParam String pageSizeName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            PDRectangle pageSize = getPageSizeFromName(pageSizeName);
            if (pageSize == null) {
                return ResponseEntity.badRequest().body("Invalid page size name.");
            }
            pdfService.setPageSize(user, fileName, pageSize);
            return ResponseEntity.ok("Page size set successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error setting page size: " + e.getMessage());
        }
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

    @PostMapping("/convertToImage")
    public ResponseEntity<?> convertPdfToImage(@RequestHeader("Authorization") String token,
                                               @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.convertPdfToImage(user, fileName);
            return ResponseEntity.ok("PDF converted to image successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error converting PDF to image: " + e.getMessage());
        }
    }

    @PostMapping("/convertToDocx")
    public ResponseEntity<?> convertPdfToDocx(@RequestHeader("Authorization") String token,
                                              @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.convertPdfToDocx(user, fileName);
            return ResponseEntity.ok("PDF converted to DOCX successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error converting PDF to DOCX: " + e.getMessage());
        }
    }

    @PostMapping("/convertToXlsx")
    public ResponseEntity<?> convertPdfToXlsx(@RequestHeader("Authorization") String token,
                                              @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.convertPdfToXlsx(user, fileName);
            return ResponseEntity.ok("PDF converted to XLSX successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error converting PDF to XLSX: " + e.getMessage());
        }
    }

    @PostMapping("/convertToPptx")
    public ResponseEntity<?> convertPdfToPptx(@RequestHeader("Authorization") String token,
                                              @RequestParam String fileName) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.convertPdfToPptx(user, fileName);
            return ResponseEntity.ok("PDF converted to PPTX successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error converting PDF to PPTX: " + e.getMessage());
        }
    }

    @PostMapping("/setPassword")
    public ResponseEntity<?> setPdfPassword(@RequestHeader("Authorization") String token,
                                            @RequestParam String fileName,
                                            @RequestParam String password) {
        try {
            String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            User user = userService.findByUsername(username);

            pdfService.setPdfPassword(user, fileName, password);
            return ResponseEntity.ok("PDF password set successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error setting password: " + e.getMessage());
        }
    }
}
