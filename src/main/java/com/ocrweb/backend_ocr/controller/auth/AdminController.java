package com.ocrweb.backend_ocr.controller.auth;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import com.ocrweb.backend_ocr.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // Create a new user
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Tạo user và tự động tạo UserProfile
        User createdUser = userService.registerUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // Get a list of all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get a single user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return ResponseEntity.ok(user);
    }

    // Update a user
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        // Lấy thông tin user hiện tại từ database
        User existingUser = userService.findById(id);

        if (existingUser == null) {
            return ResponseEntity.notFound().build(); // Nếu không tìm thấy User
        }

        // Cập nhật các thuộc tính khác
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        existingUser.setCurrentGP(user.getCurrentGP());

        // Kiểm tra và thiết lập giá trị cho status
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        } else if (existingUser.getStatus() == null) {
            existingUser.setStatus("Active"); // Giá trị mặc định nếu không được thiết lập
        }
        // Kiểm tra nếu có mật khẩu mới, thì hash lại mật khẩu
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            userService.hashAndSetPassword(existingUser, user.getPasswordHash());
        }

        // Lưu lại user đã cập nhật
        User updatedUser = userService.updateUser(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete a user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
