package com.ocrweb.backend_ocr.repository.user;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.profile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    // Phương thức để tìm kiếm UserProfile theo User
    Optional<UserProfile> findByUser(User user);
}
