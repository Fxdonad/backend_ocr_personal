package com.ocrweb.backend_ocr.controller.transaction;

import com.ocrweb.backend_ocr.dto.UserRequestInfo;
import com.ocrweb.backend_ocr.dto.UserStorageInfo;
import com.ocrweb.backend_ocr.entity.logs.ProcessingLog;
import com.ocrweb.backend_ocr.entity.transaction.GPTransactions;
import com.ocrweb.backend_ocr.dto.TransactionResponse;
import com.ocrweb.backend_ocr.dto.UserGP;
import com.ocrweb.backend_ocr.entity.user.User;
import com.ocrweb.backend_ocr.repository.logs.ProcessingLogRepository;
import com.ocrweb.backend_ocr.repository.user.UserTransactionRepository;
import com.ocrweb.backend_ocr.service.file.FileService;
import com.ocrweb.backend_ocr.service.user.UserService;
import com.ocrweb.backend_ocr.util.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;

// POST http://localhost:8081//api/transactions/process/20
// POST http://localhost:8081//api/deposit/process/20

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserTransactionRepository gpTransactionRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProcessingLogRepository processingLogRepository;

    private final long GP_TO_STORAGE_CONVERSION_RATE = 1024 * 1024;

    private final long GP_TO_REQUEST_CONVERSION_RATE = 10;

    @GetMapping("/storage-info")
    public ResponseEntity<?> getUserStorageInfo(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        long usedStorage = fileService.getTotalFileSizeByUser(user);
        long availableStorage = user.getMaxStorage() - usedStorage;
        long upgradedStorage = user.getMaxStorage() - 10 * 1024 * 1024; // Giả định dung lượng mặc định là 10MB

        return ResponseEntity.ok(new UserStorageInfo(usedStorage, availableStorage, upgradedStorage));
    }

    @PostMapping("/process/{gpCost}")
    public ResponseEntity<?> processTransaction(
            @RequestHeader("Authorization") String token,
            @PathVariable("gpCost") long gpCost){

        // Step 2: Authenticate User
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        // Validate the token
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Retrieve the user from the database
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Step 3: Check GP Balance
        if (user.getCurrentGP() < gpCost) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient GP");
        }

        // Step 4: Deduct gpCost GP from User Balance
        user.setCurrentGP(user.getCurrentGP() - gpCost);
        userService.updateUser(user);

        // Step 5: Record the GP transaction in UserTransactions
        GPTransactions gpTransaction = new GPTransactions();
        gpTransaction.setUserID(user.getUserID());
        gpTransaction.setGpUsed(gpCost);
        gpTransaction.setTransactionStatus("Success");
        gpTransaction.setTransactionDate(LocalDateTime.now());
        gpTransactionRepository.save(gpTransaction);

        // Step 6: Prepare and return the response
        TransactionResponse response = new TransactionResponse(
                user.getUsername(),
                user.getCurrentGP(),
                gpTransaction.getGpUsed(),
                gpTransaction.getTransactionDate(),
                gpTransaction.getTransactionStatus()
        );

        // Ghi log vào ProcessingLogs
        ProcessingLog log = new ProcessingLog();
        log.setUser(user);
        log.setActionType("GP Transaction");
        log.setActionDate(LocalDateTime.now());
        log.setIsSuccess(true);
        processingLogRepository.save(log);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/deposit/{gpAmount}")
    public ResponseEntity<?> depositTransaction(
            @RequestHeader("Authorization") String token,
            @PathVariable("gpAmount") long gpAmount){

        // Step 2: Authenticate User
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        // Validate the token
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Retrieve the user from the database
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Step 4: Add gpCost GP from User Balance
        user.setCurrentGP(user.getCurrentGP() + gpAmount);
        userService.updateUser(user);

        // Step 5: Record the GP transaction in UserTransactions
        GPTransactions gpTransaction = new GPTransactions();
        gpTransaction.setUserID(user.getUserID());
        gpTransaction.setGpUsed(gpAmount);
        gpTransaction.setTransactionStatus("Success");
        gpTransaction.setTransactionDate(LocalDateTime.now());
        gpTransactionRepository.save(gpTransaction);

        // Step 6: Prepare and return the response
        TransactionResponse response = new TransactionResponse(
                user.getUsername(),
                user.getCurrentGP(),
                gpTransaction.getGpUsed(),
                gpTransaction.getTransactionDate(),
                gpTransaction.getTransactionStatus()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/gpUser")
    public ResponseEntity<?> processTransaction(
            @RequestHeader("Authorization") String token){

        // Step 2: Authenticate User
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        // Validate the token
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(jwt, userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        // Retrieve the user from the database
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Step 6: Prepare and return the response
        UserGP UserGP = new UserGP(
                user.getUsername(),
                user.getCurrentGP()
        );

        return ResponseEntity.ok(UserGP);
    }

    @PostMapping("/upgrade-storage/{gpAmount}")
    public ResponseEntity<?> upgradeStorage(@RequestHeader("Authorization") String token,
                                            @PathVariable("gpAmount") long gpAmount) {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        long storageIncrease = gpAmount * GP_TO_STORAGE_CONVERSION_RATE;

        if (user.getCurrentGP() < gpAmount) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient GP to upgrade storage");
        }

        // Deduct GP from user and add storage capacity
        user.setCurrentGP(user.getCurrentGP() - gpAmount);
        user.setMaxStorage(user.getMaxStorage() + storageIncrease);
        userService.updateUser(user);

        return ResponseEntity.ok("Storage upgraded successfully.");
    }

    @GetMapping("/request-info")
    public ResponseEntity<UserRequestInfo> getUserRequestInfo(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserRequestInfo requestInfo = new UserRequestInfo(
                user.getTotalRequests(),
                user.getRemainingRequests(),
                user.getUsedRequests(),
                user.getUpgradedRequests()
        );

        return ResponseEntity.ok(requestInfo);
    }

    @PostMapping("/upgrade-requests/{gpAmount}")
    public ResponseEntity<?> upgradeRequests(@RequestHeader("Authorization") String token,
                                             @PathVariable("gpAmount") long gpAmount) {
        String jwt = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwt);

        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        int additionalRequests = (int) (gpAmount * GP_TO_REQUEST_CONVERSION_RATE);

        if (user.getCurrentGP() < gpAmount) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Insufficient GP to upgrade OCR requests");
        }

        // Deduct GP and upgrade requests
        user.setCurrentGP(user.getCurrentGP() - gpAmount);
        user.upgradeRequests(additionalRequests);
        userService.updateUser(user);

        GPTransactions gpTransaction = new GPTransactions();
        gpTransaction.setUserID(user.getUserID());
        gpTransaction.setGpUsed(gpAmount);
        gpTransaction.setTransactionStatus("Success");
        gpTransaction.setTransactionDate(LocalDateTime.now());
        gpTransactionRepository.save(gpTransaction);

        ProcessingLog log = new ProcessingLog();
        log.setUser(user);
        log.setActionType("Upgrade OCR Requests");
        log.setActionDate(LocalDateTime.now());
        log.setIsSuccess(true);
        processingLogRepository.save(log);

        return ResponseEntity.ok("OCR requests upgraded successfully.");
    }
}
