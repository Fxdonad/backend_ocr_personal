package com.ocrweb.backend_ocr.repository.user;
import com.ocrweb.backend_ocr.entity.transaction.GPTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTransactionRepository extends JpaRepository<GPTransactions, Integer> {
    // You can add custom query methods here if needed
}