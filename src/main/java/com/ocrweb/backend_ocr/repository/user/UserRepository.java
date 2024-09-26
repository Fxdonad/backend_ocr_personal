package com.ocrweb.backend_ocr.repository.user;

import com.ocrweb.backend_ocr.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
