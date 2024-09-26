package com.ocrweb.backend_ocr.repository.user;

import com.ocrweb.backend_ocr.entity.user.UserQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuestionRepository extends JpaRepository<UserQuestion, Integer> {
    List<UserQuestion> findByUser_UserID(Integer userID);

    @Query("SELECT q FROM UserQuestion q LEFT JOIN FETCH q.answers")
    List<UserQuestion> findAllWithAnswers();
}
