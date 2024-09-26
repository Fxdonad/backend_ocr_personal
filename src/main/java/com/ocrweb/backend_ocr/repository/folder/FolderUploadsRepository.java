package com.ocrweb.backend_ocr.repository.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ocrweb.backend_ocr.entity.file.FolderUploads;
import com.ocrweb.backend_ocr.entity.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderUploadsRepository extends JpaRepository<FolderUploads, Integer> {
    List<FolderUploads> findByUser(User user);

    @Query("SELECT f FROM FolderUploads f WHERE f.folderID = :folderID AND f.user = :user")
    Optional<FolderUploads> findByIdAndUser(@Param("folderID") Integer folderID, @Param("user") User user);
}
