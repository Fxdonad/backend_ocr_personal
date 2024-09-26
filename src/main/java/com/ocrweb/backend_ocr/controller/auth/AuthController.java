package com.ocrweb.backend_ocr.controller.auth;

import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import com.ocrweb.backend_ocr.service.email.EmailService;
import com.ocrweb.backend_ocr.service.logs.ProcessingLogService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProcessingLogService processingLogService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Kiểm tra trùng username
            if (userService.findByUsername(user.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
            // Kiểm tra trùng email
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        try {
            // Kiểm tra trùng username
            if (userService.findByUsername(user.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
            // Kiểm tra trùng email
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            user.setRole("ADMIN");
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            // Kiểm tra thông tin đăng nhập (username và password)
            userService.validateUserCredentials(user.getUsername(), user.getPasswordHash());

            // Thực hiện xác thực bằng AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPasswordHash())
            );

            // Tạo JWT sau khi xác thực thành công
            final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("User logged in successfully: {}", user.getUsername());
            return ResponseEntity.ok(jwt);

        } catch (UsernameNotFoundException e) {
            logger.error("User not found: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: User not found");
        } catch (BadCredentialsException e) {
            logger.error("Bad credentials for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: Incorrect username or password");
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body("No user found with this email address.");
        }

        // Generate a new temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        userService.updateUser(user);

        // Send email with the new temporary password
        String subject = "Password Reset Request";
        String text = "Your temporary password is: " + tempPassword + "\nPlease change it after logging in.";
        emailService.sendSimpleMessage(user.getEmail(), subject, text);

        // Log the action
        ProcessingLog log = new ProcessingLog();
        log.setUser(user);
        log.setActionType("Password Reset");
        log.setActionDetails("User requested password reset. A temporary password was sent to the email.");
        log.setActionDate(LocalDateTime.now());
        log.setIsSuccess(true);
        processingLogService.saveProcessingLog(log);

        return ResponseEntity.ok("A temporary password has been sent to your email.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            // Thông thường, bạn có thể blacklist token này hoặc thực hiện một hành động khác để vô hiệu hóa token.
            String jwt = token.replace("Bearer ", "");
            jwtUtil.invalidateToken(jwt); // Giả sử bạn có một method invalidateToken

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during logout: " + e.getMessage());
        }
    }

    @GetMapping("/user-details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String token) {
        try {
            // Lấy JWT token từ header Authorization
            String jwt = token.replace("Bearer ", "");

            // Trích xuất username từ JWT token
            String username = jwtUtil.extractUsername(jwt);

            // Lấy thông tin chi tiết của user từ username
            User user = userService.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }

            // Trả về thông tin chi tiết của user
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
