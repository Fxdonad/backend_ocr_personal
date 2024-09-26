package com.ocrweb.backend_ocr.repository.user;

import com.ocrweb.backend_ocr.entity.user.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Integer> {
    // Additional query methods can be added here if needed
}
