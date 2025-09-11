package com.prp.authservice.service;

import com.prp.authservice.model.User;
import com.prp.commonconfig.exception.InvalidOtpException;
import com.prp.commonconfig.exception.UnauthorizedException;
import com.prp.commonconfig.exception.UserAlreadyExistsException;
import com.prp.authservice.model.ApiResponse;
import com.prp.authservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFlowService {

    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expiration.short}")
    private long shortExpiry;

    @Value("${jwt.expiration.long}")
    private long longExpiry;


public String requestOtp(String phone, HttpServletResponse response) {
    String sessionToken = UUID.randomUUID().toString();
    otpService.storeOtp(phone, "phone", sessionToken);

    String jwtSessionToken = jwtService.generateSessionToken(sessionToken, shortExpiry);
    response.setHeader("Authorization", "Bearer " + jwtSessionToken);

    return jwtSessionToken;
}


// Verify phone OTP
public String verifyPhoneOtp(String otp, HttpServletRequest request) {
    String sessionToken = JwtService.extractSessionToken(request, jwtService);
    if (sessionToken == null) throw new UnauthorizedException("Invalid or expired session token");

    otpService.verifyOtp(sessionToken, otp, "phone"); // unified verify

    return jwtService.generateAccessToken(sessionToken);
}







public String sendEmailVerification(String email, HttpServletRequest request, HttpServletResponse response) {
    String sessionToken = JwtService.extractSessionToken(request, jwtService);
    if (sessionToken == null) throw new UnauthorizedException("Invalid or expired session token");

    otpService.storeOtp(email, "email", sessionToken);

    String jwtSessionToken = jwtService.generateSessionToken(sessionToken, shortExpiry);
    response.setHeader("Authorization", "Bearer " + jwtSessionToken);

    return jwtSessionToken;
}


// Verify email OTP
public String verifyEmailOtp(String otp, HttpServletRequest request) {
    String sessionToken = JwtService.extractSessionToken(request, jwtService);
    if (sessionToken == null) throw new UnauthorizedException("Invalid or expired session token");

    otpService.verifyOtp(sessionToken, otp, "email"); // unified verify
    return jwtService.generateAccessToken(sessionToken);
}



public User signup(User user, HttpServletRequest request, HttpServletResponse response) {
    String sessionToken = JwtService.extractSessionToken(request, jwtService);
    if (sessionToken == null) throw new UnauthorizedException("Invalid or expired token");

    String phoneVerified = redisTemplate.opsForValue().get("verified:phone:" + sessionToken);
    String emailVerified = redisTemplate.opsForValue().get("verified:email:" + sessionToken);

    if (!"true".equals(phoneVerified) || !"true".equals(emailVerified)) {
        throw new RuntimeException("Phone or email not verified yet");
    }

    User existing = authService.getUserByPhone(user.getPhoneNumber());
    if (existing != null) throw new UserAlreadyExistsException("User already registered");

    User newUser = new User();
    newUser.setFullName(user.getFullName());
    newUser.setPhoneNumber(user.getPhoneNumber());
    newUser.setEmail(user.getEmail());
    newUser.setVerified(true);  // Set verified true
    newUser.setActive(true);    // Set active true

    userRepository.save(newUser);

    String tokenNewUser = jwtService.generateUserToken(newUser.getId(), longExpiry);
    response.setHeader("Authorization", "Bearer " + tokenNewUser);

    otpService.deleteOtp(sessionToken, "phone");
    otpService.deleteOtp(sessionToken, "email");

    return newUser;
}



    // -------------------- MPIN -------------------- //
    public void createMpin(String mpin, HttpServletRequest request) {
        String userId = JwtService.extractUserIdFromToken(request, jwtService);
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized: Invalid or expired token");
        }

        User user = authService.getUserById(userId);
        authService.updateMpin(user, mpin);
    }

    public User verifyMpin(String mpin, HttpServletRequest request, HttpServletResponse response) {
        String userId = JwtService.extractUserIdFromToken(request, jwtService);
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized: Invalid or expired token");
        }

        User user = authService.getUserById(userId);
        if (!authService.verifyMpin(user, mpin)) {
            throw new UnauthorizedException("Invalid MPIN");
        }

        String accessToken = jwtService.generateUserToken(user.getId(), longExpiry);
        response.setHeader("Authorization", "Bearer " + accessToken);

        return user;
    }

    // -------------------- LOGOUT -------------------- //
    public void logout(HttpServletRequest request) {
        String token = JwtService.extractToken(request);
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new UnauthorizedException("Unauthorized: Invalid or expired token");
        }

        long expiry = jwtService.getTokenExpiry(token);
        redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(expiry));
    }
}
