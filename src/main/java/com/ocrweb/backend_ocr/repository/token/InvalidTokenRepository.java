package com.ocrweb.backend_ocr.repository.token;

import com.ocrweb.backend_ocr.entity.token.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Long> {
    Optional<InvalidToken> findByToken(String token);
}
