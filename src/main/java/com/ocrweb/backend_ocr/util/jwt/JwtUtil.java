package com.ocrweb.backend_ocr.util.jwt;

import com.ocrweb.backend_ocr.entity.token.InvalidToken;
import com.ocrweb.backend_ocr.filter.JwtRequestFilter;
import com.ocrweb.backend_ocr.repository.token.InvalidTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private InvalidTokenRepository invalidTokenRepository;

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is missing or empty");
        }

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 giờ
                .signWith(secretKey)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isTokenExpired = isTokenExpired(token);
        boolean isTokenBlacklisted = isTokenBlacklisted(token);

        if (username.equals(userDetails.getUsername()) && !isTokenExpired && !isTokenBlacklisted) {
            return true;
        } else {
            logger.debug("JWT validation failed for user: " + username + ", isTokenExpired: " + isTokenExpired + ", isTokenBlacklisted: " + isTokenBlacklisted);
            return false;
        }
    }

    public void invalidateToken(String token) {
        Date expirationDate = extractExpiration(token);
        InvalidToken invalidToken = new InvalidToken(token, expirationDate);
        invalidTokenRepository.save(invalidToken);
    }

    private boolean isTokenBlacklisted(String token) {
        Optional<InvalidToken> invalidToken = invalidTokenRepository.findByToken(token);
        boolean isBlacklisted = invalidToken.isPresent();
        if (isBlacklisted) {
            logger.debug("JWT token is blacklisted: " + token);
        }
        return isBlacklisted;
    }

    public Integer extractUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userID", Integer.class); // Assuming you store userID in JWT claims
    }
}
