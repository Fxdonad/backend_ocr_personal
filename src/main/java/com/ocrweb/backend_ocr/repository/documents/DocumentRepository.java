package com.ocrweb.backend_ocr.repository.documents;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.documents.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByUser(User user);
    List<Document> findByDocumentIDInAndUser(List<Integer> ids, User user);
}
