package com.ocrweb.backend_ocr.repository.folder;

import com.ocrweb.backend_ocr.entity.file.FolderFiles;
import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderFilesRepository extends JpaRepository<FolderFiles, Integer> {

    // Tìm file theo FolderUploads và UploadedFiles
    Optional<FolderFiles> findByFolderUploadsAndUploadedFiles(FolderUploads folderUploads, UploadedFiles uploadedFiles);

    // Tìm tất cả các file trong một folder cụ thể
    List<FolderFiles> findByFolderUploads(FolderUploads folderUploads);

    void deleteByUploadedFiles(UploadedFiles uploadedFiles);


}
