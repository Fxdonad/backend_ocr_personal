package com.ocrweb.backend_ocr.service.file;

import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.repository.file.UploadedFileRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderFilesRepository;
import com.ocrweb.backend_ocr.repository.folder.FolderUploadsRepository;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import com.ocrweb.backend_ocr.repository.documents.DocumentRepository;
import com.ocrweb.backend_ocr.service.file.pdf.PdfThumbnailService;
import com.ocrweb.backend_ocr.service.converter.ImageToBase64Service;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import com.ocrweb.backend_ocr.service.pdf.PdfService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
public class FileService {

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    @Autowired
    private ImageToBase64Service imageToBase64Service;

    @Autowired
    private PdfThumbnailService pdfThumbnailService;

    @Autowired
    private ProcessingLogService processingLogService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private FolderFilesRepository folderFilesRepository;

    @Autowired
    private FolderUploadsRepository folderUploadsRepository;

    private final String uploadDir;

    public FileService() {
        Dotenv dotenv = Dotenv.load();
        this.uploadDir = dotenv.get("FOLDER_OUTPUT_DIRECTORY");
    }

    private final long GP_TO_STORAGE_CONVERSION_RATE = 2 * 1024 * 1024;

    private final long MAX_USER_STORAGE = 1000 * 1024 * 1024; // Giới hạn dung lượng
    private final int MAX_FILES_IN_FOLDER = 10; // Giới hạn số lượng file trong một folder

    @Transactional
    public UploadedFiles saveFile(User user, MultipartFile file) throws IOException {
        long userTotalSize = getTotalFileSizeByUser(user);

        if (userTotalSize + file.getSize() > MAX_USER_STORAGE) {
            throw new RuntimeException("User has exceeded the maximum allowed storage.");
        }

        return saveFileInternal(user, file); // Thực hiện lưu file nếu đủ điều kiện
    }

    @Transactional
    public UploadedFiles saveFileInternal(User user, MultipartFile file) throws IOException {
        // Kiểm tra loại file
        String mimeType = file.getContentType();
        String fileType = mapMimeTypeToFileType(mimeType);

        // Tiếp tục xử lý việc lưu file như đã mô tả ở trên
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.write(filePath, file.getBytes());

        // Tạo đối tượng UploadedFiles và lưu vào database
        UploadedFiles uploadedFile = new UploadedFiles();
        uploadedFile.setUser(user);
        uploadedFile.setFileName(uniqueFileName);
        uploadedFile.setFileType(fileType);
        uploadedFile.setFileSize(file.getSize());
        uploadedFile.setFilePath(filePath.toString());
        uploadedFile.setUploadDate(LocalDateTime.now());
        uploadedFile.setThumbnail(createThumbnail(file, fileType));

        // Lưu đối tượng UploadedFiles trước khi sử dụng nó trong Document
        uploadedFile = uploadedFileRepository.save(uploadedFile);

        // Ghi dữ liệu vào bảng Document
        Document document = new Document();
        document.setUser(user);
        document.setFile(uploadedFile);  // Liên kết với UploadedFiles đã được lưu
        document.setDocumentName(file.getOriginalFilename());
        document.setDocumentType(uploadedFile.getFileType());
        document.setUploadDate(uploadedFile.getUploadDate());
        document.setStatus("Active");
        document.setFilePath(uploadedFile.getFilePath());  // Gán filePath từ UploadedFiles vào Document

        if ("PDF".equals(fileType)) {
            uploadedFile.setProcessed(true);
            uploadedFile.setProcessedDate(LocalDateTime.now());
        } else if ("JPG".equals(fileType) || "PNG".equals(fileType)) {
            String base64 = imageToBase64Service.convertImageToBase64Upload(file);
            document.setBase64(base64);
            uploadedFile.setProcessed(true);
            uploadedFile.setProcessedDate(LocalDateTime.now());
        }

        documentRepository.save(document);
        entityManager.flush();  // Đảm bảo các thay đổi được đồng bộ hóa

        // Ghi dữ liệu vào bảng ProcessingLogs
        ProcessingLog processingLog = new ProcessingLog();
        processingLog.setUploadedFile(uploadedFile);
        processingLog.setUser(user);
        processingLog.setActionType("Upload");
        processingLog.setActionDate(uploadedFile.getProcessedDate() != null ? uploadedFile.getProcessedDate() : LocalDateTime.now());
        processingLog.setIsSuccess(true);

        processingLogRepository.save(processingLog);

        // Nạp lại UploadedFiles để đảm bảo document không bị null
        uploadedFile = uploadedFileRepository.findById(uploadedFile.getFileID()).orElseThrow(() ->
                new RuntimeException("Uploaded file not found after save"));

        return uploadedFile;
    }

    @Transactional
    public void deleteFile(User user, Integer fileId) {
        UploadedFiles file = getFileByIdAndUser(fileId, user);

        // Delete all associated records in FolderFiles
        folderFilesRepository.deleteByUploadedFiles(file);

        // Log the deletion action
        processingLogService.logProcessing(file, user, "Delete", true, null);

        // Delete the file record from UploadedFiles
        uploadedFileRepository.delete(file);
    }

    @Transactional
    public UploadedFiles updateFile(User user, Integer fileId, MultipartFile file) throws IOException {
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

        return uploadedFileRepository.save(existingFile);
    }

    private String createThumbnail(MultipartFile file, String fileType) throws IOException {
        if ("JPG".equals(fileType) || "PNG".equals(fileType)) {
            // Đối với hình ảnh, sử dụng dịch vụ để tạo Base64 của thumbnail
            BufferedImage image = ImageIO.read(file.getInputStream());
            BufferedImage thumbnailImage = createResizedImage(image, 100, 100);
            return imageToBase64Service.convertBufferedImageToBase64(thumbnailImage, fileType.toLowerCase());
        } else if ("PDF".equals(fileType)) {
            // Đối với PDF, tạo hình ảnh từ trang đầu tiên
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

    public UploadedFiles getFileByIdAndUser(Integer fileId, User user) {
        return uploadedFileRepository.findByFileIDAndUser(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found or you do not have permission to access it."));
    }

    public List<UploadedFiles> getFilesByUser(User user) {
        return uploadedFileRepository.findByUser(user);
    }


    public long getTotalFileSizeByUser(User user) {
        return uploadedFileRepository.findByUser(user)
                .stream()
                .mapToLong(UploadedFiles::getFileSize)
                .sum();
    }

    public long getTotalFileSizeInFolder(User user, Integer folderId) {
        FolderUploads folder = folderUploadsRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new RuntimeException("Folder not found or you do not have permission to access it."));
        return folder.getFolderFiles()
                .stream()
                .mapToLong(folderFile -> folderFile.getUploadedFiles().getFileSize())
                .sum();
    }
}
