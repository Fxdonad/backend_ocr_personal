package com.ocrweb.backend_ocr.controller.file;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.repository.folder.FolderFilesRepository;
import com.ocrweb.backend_ocr.service.file.FileService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/auth/user/files")
@CrossOrigin(origins = "http://10.0.2.2:8081")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Upload file
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestHeader("Authorization") String token,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        UploadedFiles uploadedFile = fileService.saveFile(user, file);

        // Refresh the uploadedFile entity to load associated document
        uploadedFile = fileService.getFileByIdAndUser(uploadedFile.getFileID(), user);

        return ResponseEntity.ok(uploadedFile);
    }

    // Delete file
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@RequestHeader("Authorization") String token,
                                        @PathVariable Integer fileId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        fileService.deleteFile(user, fileId);
        return ResponseEntity.ok("File deleted successfully.");
    }

    // Update file
    @PutMapping("/{fileId}")
    public ResponseEntity<UploadedFiles> updateFile(@RequestHeader("Authorization") String token,
                                                    @PathVariable Integer fileId,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        UploadedFiles updatedFile = fileService.updateFile(user, fileId, file);
        return ResponseEntity.ok(updatedFile);
    }


    // Lấy danh sách file của user
    @GetMapping("/list")
    public ResponseEntity<List<UploadedFiles>> getUserFiles(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        List<UploadedFiles> files = fileService.getFilesByUser(user);
        return ResponseEntity.ok(files);
    }

    // Lấy chi tiết file theo ID
    @GetMapping("/{fileId}")
    public ResponseEntity<UploadedFiles> getFileById(@RequestHeader("Authorization") String token,
                                                     @PathVariable Integer fileId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        UploadedFiles file = fileService.getFileByIdAndUser(fileId, user);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/total-size")
    public ResponseEntity<Long> getTotalFileSizeByUser(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        long totalSize = fileService.getTotalFileSizeByUser(user);
        return ResponseEntity.ok(totalSize);
    }
}
