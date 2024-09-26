package com.ocrweb.backend_ocr.controller.folder;

import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.file.FileService;
import com.ocrweb.backend_ocr.service.folder.FolderService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/user/folders")
public class FolderController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FolderService folderService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FolderUploads> uploadFolder(@RequestHeader("Authorization") String token,
                                                      @RequestParam("folderName") String folderName,
                                                      @RequestParam("files") List<MultipartFile> files) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        FolderUploads folderUploads = folderService.saveFolder(user, folderName, files);
        return ResponseEntity.ok(folderUploads);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderUploads> getFolderWithFiles(@RequestHeader("Authorization") String token,
                                                            @PathVariable Integer folderId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        FolderUploads folderUploads = folderService.getFolderWithFiles(user, folderId);
        return ResponseEntity.ok(folderUploads);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FolderUploads>> getAllFolders(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        List<FolderUploads> folderUploads = folderService.getAllFoldersByUser(user);
        return ResponseEntity.ok(folderUploads);
    }


    @DeleteMapping("/{folderId}")
    public ResponseEntity<?> deleteFolder(@RequestHeader("Authorization") String token,
                                          @PathVariable Integer folderId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        folderService.deleteFolder(user, folderId);
        return ResponseEntity.ok("Folder deleted successfully.");
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<FolderUploads> updateFolder(@RequestHeader("Authorization") String token,
                                                      @PathVariable Integer folderId,
                                                      @RequestParam("folderName") String folderName) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        FolderUploads updatedFolder = folderService.updateFolder(user, folderId, folderName);
        return ResponseEntity.ok(updatedFolder);
    }

    @PostMapping("/{folderId}/upload")
    public ResponseEntity<FolderUploads> uploadFilesToExistingFolder(@RequestHeader("Authorization") String token,
                                                                     @PathVariable Integer folderId,
                                                                     @RequestParam("files") List<MultipartFile> files) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        FolderUploads updatedFolder = folderService.addFilesToFolder(user, folderId, files);
        return ResponseEntity.ok(updatedFolder);
    }

    @GetMapping("/{folderId}/files")
    public ResponseEntity<List<UploadedFiles>> getFilesInFolder(@RequestHeader("Authorization") String token,
                                                                @PathVariable Integer folderId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        List<UploadedFiles> files = folderService.getFilesInFolder(user, folderId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{folderId}/files/{fileId}")
    public ResponseEntity<?> deleteFileInFolder(@RequestHeader("Authorization") String token,
                                                @PathVariable Integer folderId,
                                                @PathVariable Integer fileId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        folderService.deleteAllFilesWithFileId(user, folderId, fileId);
        return ResponseEntity.ok("All files with the same file ID deleted successfully.");
    }

    @PutMapping("/{folderId}/files/{fileId}")
    public ResponseEntity<UploadedFiles> updateFileInFolder(@RequestHeader("Authorization") String token,
                                                            @PathVariable Integer folderId,
                                                            @PathVariable Integer fileId,
                                                            @RequestParam("file") MultipartFile file) throws IOException {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        UploadedFiles updatedFile = folderService.updateFileInFolder(user, folderId, fileId, file);
        return ResponseEntity.ok(updatedFile);
    }

    @GetMapping("/{folderId}/total-size")
    public ResponseEntity<Long> getTotalFileSizeInFolder(@RequestHeader("Authorization") String token,
                                                         @PathVariable Integer folderId) {
        String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
        User user = userService.findByUsername(username);

        long totalSize = fileService.getTotalFileSizeInFolder(user, folderId);
        return ResponseEntity.ok(totalSize);
    }
}

