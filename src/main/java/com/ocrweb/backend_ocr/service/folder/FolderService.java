package com.ocrweb.backend_ocr.service.folder;

import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.entity.file.FolderFiles;
import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.documents.DocumentRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderFilesRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderUploadsRepository;
import com.ocrweb.backend_ocr.repository.file.UploadedFileRepository;
import com.ocrweb.backend_ocr.service.actions.UserActionService;
import com.ocrweb.backend_ocr.service.converter.ImageToBase64Service;
import com.ocrweb.backend_ocr.service.file.pdf.PdfThumbnailService;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import io.github.cdimascio.dotenv.Dotenv;


@Service
public class FolderService {

    @Autowired
    private FolderUploadsRepository folderUploadsRepository;

    @Autowired
    private FolderFilesRepository folderFilesRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ImageToBase64Service imageToBase64Service;

    @Autowired
    private PdfThumbnailService pdfThumbnailService;

    @Autowired
    private ProcessingLogService processingLogService;

    @Autowired
    private UserActionService userActionService;

    private final String uploadDir;

    public FolderService() {
        Dotenv dotenv = Dotenv.load();
        this.uploadDir = dotenv.get("FOLDER_OUTPUT_DIRECTORY");
    }

    private final long MAX_USER_STORAGE = 10 * 1024 * 1024; // Giới hạn dung lượng là 500MB
    private final int MAX_FILES_IN_FOLDER = 10; // Giới hạn số lượng file trong một folder

    @Transactional
    public FolderUploads saveFolder(User user, String folderName, List<MultipartFile> files) throws IOException {
        if (files.size() > MAX_FILES_IN_FOLDER) {
            throw new RuntimeException("Folder exceeds the maximum allowed number of files.");
        }

        return saveFolderInternal(user, folderName, files); // Thực hiện lưu folder nếu đủ điều kiện
    }

    @Transactional
    public FolderUploads saveFolderInternal(User user, String folderName, List<MultipartFile> files) throws IOException {
        long totalFileSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        long userTotalSize = uploadedFileRepository.findByUser(user).stream().mapToLong(UploadedFiles::getFileSize).sum();

        if (userTotalSize + totalFileSize > user.getMaxStorage()) {
            throw new RuntimeException("Uploading these files will exceed the maximum allowed storage.");
        }

        FolderUploads folderUploads = new FolderUploads();
        folderUploads.setUser(user);
        folderUploads.setFolderName(folderName);
        folderUploads.setUploadDate(LocalDateTime.now());
        folderUploads.setProcessed(true);
        folderUploads.setProcessedDate(LocalDateTime.now());
        folderUploads = folderUploadsRepository.save(folderUploads);

        for (MultipartFile file : files) {
            UploadedFiles uploadedFile = saveFile(user, file, folderUploads);
            FolderFiles folderFiles = new FolderFiles();
            folderFiles.setFolderUploads(folderUploads);
            folderFiles.setUploadedFiles(uploadedFile);
            folderFilesRepository.save(folderFiles);

            userActionService.logUserAction(user, uploadedFile.getDocument(), "Upload", true);
        }

        return folderUploads;
    }

    @Transactional
    public UploadedFiles saveFile(User user, MultipartFile file, FolderUploads folderUploads) throws IOException {
        String mimeType = file.getContentType();
        String fileType = mapMimeTypeToFileType(mimeType);

        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.write(filePath, file.getBytes());

        UploadedFiles uploadedFile = new UploadedFiles();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(uniqueFileName);
        uploadedFile.setFileType(fileType);
        uploadedFile.setFileSize(file.getSize());
        uploadedFile.setFilePath(filePath.toString());
        uploadedFile.setUploadDate(LocalDateTime.now());
        uploadedFile.setThumbnail(createThumbnail(file, fileType));

        uploadedFile = uploadedFileRepository.save(uploadedFile);

        Document document = new Document();
        document.setUser(user);
        document.setFile(uploadedFile);
        document.setDocumentName(file.getOriginalFilename());
        document.setDocumentType(uploadedFile.getFileType());
        document.setUploadDate(uploadedFile.getUploadDate());
        document.setStatus("Active");
        document.setFilePath(uploadedFile.getFilePath());

        if ("PDF".equals(uploadedFile.getFileType())) {
            uploadedFile.setProcessed(true);
            uploadedFile.setProcessedDate(LocalDateTime.now());
            document.setBase64(null);
        } else if ("JPG".equals(uploadedFile.getFileType()) || "PNG".equals(uploadedFile.getFileType())) {
            String base64 = imageToBase64Service.convertImageToBase64Upload(file);
            document.setBase64(base64);
            uploadedFile.setProcessed(true);
            uploadedFile.setProcessedDate(LocalDateTime.now());
        }

        documentRepository.save(document);
        uploadedFile = uploadedFileRepository.save(uploadedFile);

        return uploadedFile;
    }

    @Transactional
    public List<FolderUploads> getAllFoldersByUser(User user) {
        // Fetch all folders uploaded by the user
        return folderUploadsRepository.findByUser(user);
    }


    @Transactional
    public FolderUploads getFolderWithFiles(User user, Integer folderId) {
        FolderUploads folderUploads = getFolderByIdAndUser(folderId, user);

        // Eagerly load the files in the folder
        List<FolderFiles> folderFiles = folderFilesRepository.findByFolderUploads(folderUploads);
        folderFiles.forEach(file -> file.getUploadedFiles().getFileName()); // Force loading the files

        folderUploads.setFolderFiles(folderFiles); // Assuming a setFolderFiles method exists

        return folderUploads;
    }

    @Transactional
    public void deleteFolder(User user, Integer folderId) {
        FolderUploads folder = getFolderByIdAndUser(folderId, user);
        folderUploadsRepository.delete(folder);
    }

    @Transactional
    public FolderUploads updateFolder(User user, Integer folderId, String folderName) {
        FolderUploads existingFolder = getFolderByIdAndUser(folderId, user);

        // Cập nhật thông tin của folder
        existingFolder.setFolderName(folderName);
        return folderUploadsRepository.save(existingFolder);
    }

    @Transactional
    public List<UploadedFiles> getFilesInFolder(User user, Integer folderId) {
        FolderUploads folder = getFolderByIdAndUser(folderId, user);
        return folderFilesRepository.findByFolderUploads(folder).stream()
                .map(FolderFiles::getUploadedFiles)
                .toList();
    }

    @Transactional
    public FolderUploads addFilesToFolder(User user, Integer folderId, List<MultipartFile> files) throws IOException {
        FolderUploads folderUploads = getFolderByIdAndUser(folderId, user);

        for (MultipartFile file : files) {
            UploadedFiles uploadedFile = saveFile(user, file, folderUploads);
            FolderFiles folderFiles = new FolderFiles();
            folderFiles.setFolderUploads(folderUploads);
            folderFiles.setUploadedFiles(uploadedFile);
            folderFilesRepository.save(folderFiles);

            // Ghi log (log chỉ được ghi một lần khi file được upload)
            userActionService.logUserAction(user, uploadedFile.getDocument(), "Upload", true);
        }

        return folderUploads;
    }

    @Transactional
    public void deleteFileInFolder(User user, Integer folderId, Integer fileId) {
        FolderUploads folder = getFolderByIdAndUser(folderId, user);
        UploadedFiles file = getFileByIdAndUser(fileId, user);
        FolderFiles folderFile = folderFilesRepository.findByFolderUploadsAndUploadedFiles(folder, file)
                .orElseThrow(() -> new RuntimeException("File not found in the folder or you do not have permission to delete it."));

        processingLogService.logProcessing(file, user, "Delete", true, null);

        folderFilesRepository.delete(folderFile);

        uploadedFileRepository.delete(file);
    }

    @Transactional
    public void deleteAllFilesWithFileId(User user, Integer folderId, Integer fileId) {
        FolderUploads folder = getFolderByIdAndUser(folderId, user);
        UploadedFiles fileToDelete = getFileByIdAndUser(fileId, user);

        List<UploadedFiles> filesToDelete = uploadedFileRepository.findAllByFileID(fileToDelete.getFileID());

        for (UploadedFiles file : filesToDelete) {
            // Delete related folder files
            folderFilesRepository.deleteByUploadedFiles(file);

            // Log the deletion action
            processingLogService.logProcessing(file, user, "Delete", true, null);

            // Delete the uploaded file
            uploadedFileRepository.delete(file);
        }
    }

    @Transactional
    public UploadedFiles updateFileInFolder(User user, Integer folderId, Integer fileId, MultipartFile file) throws IOException {
        FolderUploads folder = getFolderByIdAndUser(folderId, user);
        UploadedFiles existingFile = getFileByIdAndUser(fileId, user);

        // Cập nhật thông tin file mới
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.write(filePath, file.getBytes());

        String mimeType = file.getContentType();
        String fileType = mapMimeTypeToFileType(mimeType);

        existingFile.setFileName(file.getOriginalFilename());
        existingFile.setFileType(fileType);
        existingFile.setFileSize(file.getSize());
        existingFile.setFilePath(filePath.toString());
        existingFile.setUploadDate(LocalDateTime.now());
        existingFile.setThumbnail(createThumbnail(file, fileType));

        if ("JPG".equals(fileType) || "PNG".equals(fileType)) {
            String base64 = imageToBase64Service.convertImageToBase64Upload(file);
            existingFile.setBase64(base64);
        } else {
            existingFile.setBase64(null);
        }

        Document document = existingFile.getDocument();
        document.setDocumentName(existingFile.getFileName());
        document.setDocumentType(existingFile.getFileType());
        document.setFilePath(existingFile.getFilePath());

        if ("PDF".equals(existingFile.getFileType())) {
            document.setBase64(null);
        } else if ("JPG".equals(existingFile.getFileType()) || "PNG".equals(existingFile.getFileType())) {
            document.setBase64(existingFile.getBase64());
        }

        documentRepository.save(document);
        uploadedFileRepository.save(existingFile);

        return existingFile;
    }

    private FolderUploads getFolderByIdAndUser(Integer folderId, User user) {
        return folderUploadsRepository.findById(folderId)
                .filter(folder -> folder.getUser().equals(user))
                .orElseThrow(() -> new RuntimeException("Folder not found or you do not have permission to access it."));
    }

    private UploadedFiles getFileByIdAndUser(Integer fileId, User user) {
        return uploadedFileRepository.findByFileIDAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found or you do not have permission to access it."));
    }


    private String createThumbnail(MultipartFile file, String fileType) throws IOException {
        if ("JPG".equals(fileType) || "PNG".equals(fileType)) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            BufferedImage thumbnailImage = createResizedImage(image, 100, 100);
            return imageToBase64Service.convertBufferedImageToBase64(thumbnailImage, fileType.toLowerCase());
        } else if ("PDF".equals(fileType)) {
            return pdfThumbnailService.createPdfThumbnail(file);
        }
        return null;
    }

    private BufferedImage createResizedImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        return resizedImage;
    }

    private String mapMimeTypeToFileType(String mimeType) {
        switch (mimeType) {
            case "application/pdf":
                return "PDF";
            case "image/jpeg":
                return "JPG";
            case "image/png":
                return "PNG";
            default:
                throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }
    }
}
