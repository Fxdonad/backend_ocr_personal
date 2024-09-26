package com.ocrweb.backend_ocr.controller.profile;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.profile.UserProfile;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import com.ocrweb.backend_ocr.repository.user.UserProfileRepository;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // GET /user/profile
    @GetMapping
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(jwt);
            User user = userService.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }

            return ResponseEntity.ok(user.getUserProfile());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // PUT /user/profile
    @PutMapping
    public ResponseEntity<?> createOrUpdateUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfile userProfile) {
        try {
            String jwt = token.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(jwt);
            User user = userService.findByUsername(username);

            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }

            userProfile.setUser(user);

            UserProfile existingProfile = user.getUserProfile();
            if (existingProfile != null) {
                userProfile.setProfileID(existingProfile.getProfileID());
            }

            userService.saveUserProfile(userProfile);

            return ResponseEntity.ok("User profile created/updated successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/clear")
    public ResponseEntity<String> clearUserProfile(@RequestHeader("Authorization") String token) {
        String username = extractUsernameFromToken(token);
        User user = userService.findByUsername(username);

        if (user == null || user.getUserProfile() == null) {
            throw new ResourceNotFoundException("User profile not found.");
        }

        UserProfile userProfile = user.getUserProfile();
        userProfile.setFirstName("");
        userProfile.setLastName("");
        userProfile.setAddress("");
        userProfile.setPhoneNumber("");
        userProfile.setLastLoginDate(null);

        userProfileRepository.save(userProfile);

        return ResponseEntity.ok("User profile cleared successfully");
    }

    private String extractUsernameFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return userService.extractUsername(jwt);
    }
}
