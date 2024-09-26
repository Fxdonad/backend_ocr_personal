package com.ocrweb.backend_ocr.service.documents;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.documents.Document;
import com.ocrweb.backend_ocr.repository.documents.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public Document getDocumentByIdAndUser(Integer documentId, User user) {
        return documentRepository.findById(documentId)
                .filter(document -> document.getUser().equals(user))
                .orElseThrow(() -> new RuntimeException("Document not found or you do not have permission to access it."));
    }

    public List<Document> getDocumentsByUser(User user) {
        return documentRepository.findByUser(user);
    }

    public List<Document> getDocumentsByIdsAndUser(List<Integer> documentIds, User user) {
        return documentRepository.findByDocumentIDInAndUser(documentIds, user);
    }
}
