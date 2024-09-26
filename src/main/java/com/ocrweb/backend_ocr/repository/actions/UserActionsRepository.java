package com.ocrweb.backend_ocr.repository.actions;

import com.ocrweb.backend_ocr.entity.actions.UserActions;
import com.ocrweb.backend_ocr.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserActionsRepository extends JpaRepository<UserActions, Integer> {
    List<UserActions> findByUser(User user);
}
