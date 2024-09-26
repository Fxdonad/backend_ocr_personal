package com.ocrweb.backend_ocr.repository.file;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.file.UploadedFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFiles, Integer> {
    List<UploadedFiles> findByUser(User user);
    Optional<UploadedFiles> findByFileIDAndUser(Integer fileID, User user);
    List<UploadedFiles> findAllByFileID(Integer fileID);
}
