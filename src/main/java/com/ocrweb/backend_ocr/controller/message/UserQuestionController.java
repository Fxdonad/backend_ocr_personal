package com.ocrweb.backend_ocr.controller.message;

import com.ocrweb.backend_ocr.entity.user.UserQuestion;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.repository.user.UserQuestionRepository;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class UserQuestionController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserQuestionRepository userQuestionRepository;

    @GetMapping("/all")
    public ResponseEntity<List<UserQuestion>> getAllUserQuestions(@RequestHeader("Authorization") String token) {
        // Extract JWT token and username
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        // Load UserDetails and validate the token
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Retrieve all UserQuestion entities
        List<UserQuestion> userQuestions = userQuestionRepository.findAll();

        // Return the list of UserQuestions
        return ResponseEntity.ok(userQuestions);
    }

    @GetMapping("/all-with-answers")
    public ResponseEntity<List<UserQuestion>> getAllUserQuestionsWithAnswers(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<UserQuestion> userQuestions = userQuestionRepository.findAllWithAnswers();

        // Return the list of UserQuestions along with their answers
        return ResponseEntity.ok(userQuestions);
    }

    @GetMapping("/user")
    public ResponseEntity<List<UserQuestion>> getQuestionsByUser(
            @RequestHeader("Authorization") String token) {

        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<UserQuestion> questions = userQuestionRepository.findByUser_UserID(user.getUserID());
        return ResponseEntity.ok(questions);
    }

   @PostMapping
    public ResponseEntity<UserQuestion> createQuestion(
            @RequestHeader("Authorization") String token,
            @RequestBody UserQuestion userQuestion) {

        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Set the User entity and message time
        userQuestion.setUser(user);
        userQuestion.setMessageTime(LocalDateTime.now());

        // Save the UserQuestion entity to the database
        UserQuestion savedQuestion = userQuestionRepository.save(userQuestion);

        // Return the saved entity as the response
        return ResponseEntity.ok(savedQuestion);
    }

    @GetMapping("/{questionID}")
    public ResponseEntity<UserQuestion> getQuestionById(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer questionID) {

        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserQuestion userQuestion = userQuestionRepository.findById(questionID)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return ResponseEntity.ok(userQuestion);
    }

    @PutMapping("/{questionID}")
    public ResponseEntity<UserQuestion> updateQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer questionID,
            @RequestBody UserQuestion updatedQuestion) {

        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserQuestion existingQuestion = userQuestionRepository.findById(questionID)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        existingQuestion.setMessage(updatedQuestion.getMessage());
        existingQuestion.setMessageTime(LocalDateTime.now());
        UserQuestion savedQuestion = userQuestionRepository.save(existingQuestion);
        return ResponseEntity.ok(savedQuestion);
    }

    @DeleteMapping("/{questionID}")
    public ResponseEntity<Void> deleteQuestion(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer questionID) {

        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserQuestion userQuestion = userQuestionRepository.findById(questionID)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        userQuestionRepository.delete(userQuestion);
        return ResponseEntity.noContent().build();
    }
}
