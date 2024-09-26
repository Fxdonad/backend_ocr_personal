package com.ocrweb.backend_ocr.service.user;

import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.entity.profile.UserProfile;
import com.ocrweb.backend_ocr.exception.ResourceNotFoundException;
import com.ocrweb.backend_ocr.repository.user.UserProfileRepository;
import com.ocrweb.backend_ocr.repository.user.UserRepository;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User registerUser(User user) {
        // Kiểm tra trùng username
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        // Kiểm tra trùng email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (user.getRole() == null) {
            user.setRole("User"); // Gán vai trò User nếu vai trò chưa được thiết lập
        }

        // Đảm bảo rằng status không null
        if (user.getStatus() == null) {
            user.setStatus("Active"); // Thiết lập giá trị mặc định cho status
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setStatus("Active");

        // Lưu User và tạo UserProfile tương ứng
        User savedUser = userRepository.save(user);

        // Kiểm tra xem UserProfile có tồn tại không trước khi tạo
        if (userProfileRepository.findByUser(savedUser).isEmpty()) {
            createUserProfile(savedUser);
        }

        return savedUser;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void createUserProfile(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);  // Thiết lập quan hệ one-to-one
        userProfile.setCreateDate(LocalDateTime.now()); // Thiết lập ngày tạo nếu cần thiết
        userProfileRepository.save(userProfile);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void hashAndSetPassword(User user, String rawPassword) {
        // Hash lại mật khẩu và gán vào đối tượng User
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
    }

    public User updateUser(User user) {
        User existingUser = findById(user.getUserID());
        if (existingUser == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Đảm bảo registrationDate không bị null
        user.setRegistrationDate(existingUser.getRegistrationDate());

        return userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        User user = findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.delete(user);
    }

    public void saveUserProfile(UserProfile userProfile) {
        userProfileRepository.save(userProfile);
    }

    public User findById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String extractUsername(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUsername(jwt);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(),
                getAuthority(user));
    }


    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        return authorities;
    }

    public boolean validateUserCredentials(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect username or password");
        }
        return true;
    }
}
