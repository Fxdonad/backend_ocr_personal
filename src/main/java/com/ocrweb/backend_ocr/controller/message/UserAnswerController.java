package com.ocrweb.backend_ocr.controller.message;

import com.ocrweb.backend_ocr.entity.user.UserAnswer;
import com.ocrweb.backend_ocr.entity.user.UserQuestion;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.repository.user.UserAnswerRepository;
import com.ocrweb.backend_ocr.repository.user.UserQuestionRepository;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/answers")
public class UserAnswerController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private UserQuestionRepository userQuestionRepository;

    @PostMapping("/question/{questionID}")
    public ResponseEntity<UserAnswer> createAnswer(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer questionID,
            @RequestBody UserAnswer userAnswer) {

        // Extract JWT token and username
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        // Load UserDetails and validate the token
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Find the User by username
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Find the question by ID
        UserQuestion userQuestion = userQuestionRepository.findById(questionID)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Set the User and Question to the UserAnswer entity
        userAnswer.setUser(user);
        userAnswer.setQuestion(userQuestion);
        userAnswer.setAnswerTime(LocalDateTime.now());

        // Save the UserAnswer entity to the database
        UserAnswer savedAnswer = userAnswerRepository.save(userAnswer);

        // Return the saved entity
        return ResponseEntity.ok(savedAnswer);
    }
}
