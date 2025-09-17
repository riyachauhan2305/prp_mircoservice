package com.prp.authservice.service;

// import com.arangodb.entity.UserEntity;
// import com.prp.authservice.model.User;
import com.prp.authservice.entity.UserEntity;
import com.prp.authservice.dto.SignupDto;
import com.prp.authservice.dto.UserResponseDto;
import com.prp.authservice.repository.UserRepository;
import com.prp.commonconfig.exception.UnauthorizedException;
import com.prp.commonconfig.exception.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
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
     private final ModelMapper modelMapper;

    @Value("${jwt.expiration.short}")
    private long shortExpiry;

    @Value("${jwt.expiration.long}")
    private long longExpiry;

    // Request phone OTP
    public String requestOtp(String phone, HttpServletResponse response) {
        // Validate phone number length and digits
        if (phone == null || !phone.matches("\\d{10}")) {
            return null; // Return null if validation fails
        }

        String sessionToken = UUID.randomUUID().toString();
        otpService.storeOtp(phone, "phone", sessionToken);

        String jwtSessionToken = jwtService.generateSessionToken(sessionToken, shortExpiry);
        response.setHeader("Authorization", "Bearer " + jwtSessionToken);

        return jwtSessionToken;
    }

    // Verify phone OTP
    public String verifyPhoneOtp(String otp, HttpServletRequest request) {
        String sessionToken = JwtService.extractSessionToken(request, jwtService);
        if (sessionToken == null)
            throw new UnauthorizedException("Invalid or expired session token");

        otpService.verifyOtp(sessionToken, otp, "phone"); // unified verify

        return jwtService.generateAccessToken(sessionToken);
    }

    public String sendEmailVerification(String email, HttpServletRequest request, HttpServletResponse response) {
        String sessionToken = JwtService.extractSessionToken(request, jwtService);
        if (sessionToken == null)
            throw new UnauthorizedException("Invalid or expired session token");

        otpService.storeOtp(email, "email", sessionToken);

        String jwtSessionToken = jwtService.generateSessionToken(sessionToken, shortExpiry);
        response.setHeader("Authorization", "Bearer " + jwtSessionToken);

        return jwtSessionToken;
    }

    // Verify email OTP
    public String verifyEmailOtp(String otp, HttpServletRequest request) {
        String sessionToken = JwtService.extractSessionToken(request, jwtService);
        if (sessionToken == null)
            throw new UnauthorizedException("Invalid or expired session token");

        otpService.verifyOtp(sessionToken, otp, "email"); // unified verify
        return jwtService.generateAccessToken(sessionToken);
    }


//     //SIgnup
//     public UserResponseDto signup(SignupDto signupDto, HttpServletRequest request, HttpServletResponse response) {
//     String sessionToken = JwtService.extractSessionToken(request, jwtService);
//     if (sessionToken == null)
//         throw new UnauthorizedException("Invalid or expired token");

//     String phoneVerified = redisTemplate.opsForValue().get("verified:phone:" + sessionToken);
//     String emailVerified = redisTemplate.opsForValue().get("verified:email:" + sessionToken);

//     if (!"true".equals(phoneVerified) || !"true".equals(emailVerified)) {
//         throw new RuntimeException("Phone or email not verified yet");
//     }

//     UserEntity existing = authService.getUserByPhone(signupDto.getPhoneNumber());
//     if (existing != null)
//         throw new UserAlreadyExistsException("User already registered");

//     UserEntity newUser = new UserEntity();
//     newUser.setFullName(signupDto.getFullName());
//     newUser.setPhoneNumber(signupDto.getPhoneNumber());
//     newUser.setEmail(signupDto.getEmail());
//     newUser.setVerified(true);
//     newUser.setActive(true);

//     userRepository.save(newUser);

//     String tokenNewUser = jwtService.generateUserToken(newUser.getId(), longExpiry);
//     response.setHeader("Authorization", "Bearer " + tokenNewUser);

//     otpService.deleteOtp(sessionToken, "phone");
//     otpService.deleteOtp(sessionToken, "email");

//     return modelMapper.map(newUser, UserResponseDto.class);
// }


// Signup
public UserResponseDto signup(SignupDto signupDto, HttpServletRequest request, HttpServletResponse response) {
    System.out.println("=== START SIGNUP ===");

    String sessionToken = JwtService.extractSessionToken(request, jwtService);
    System.out.println("Extracted sessionToken: " + sessionToken);
    if (sessionToken == null) {
        System.out.println("Session token is null -> unauthorized");
        throw new UnauthorizedException("Invalid or expired token");
    }

    String phoneVerified = redisTemplate.opsForValue().get("verified:phone:" + sessionToken);
    String emailVerified = redisTemplate.opsForValue().get("verified:email:" + sessionToken);
    System.out.println("Phone verified: " + phoneVerified);
    System.out.println("Email verified: " + emailVerified);

    if (!"true".equals(phoneVerified) || !"true".equals(emailVerified)) {
        System.out.println("Either phone or email not verified, throwing exception");
        throw new RuntimeException("Phone or email not verified yet");
    }

    UserEntity existing = authService.getUserByPhone(signupDto.getPhoneNumber());
    System.out.println("Existing user found: " + (existing != null));
    if (existing != null) {
        throw new UserAlreadyExistsException("User already registered");
    }

    UserEntity newUser = new UserEntity();
    newUser.setFullName(signupDto.getFullName());
    newUser.setPhoneNumber(signupDto.getPhoneNumber());
    newUser.setEmail(signupDto.getEmail());
    newUser.setVerified(true);
    newUser.setActive(true);

    userRepository.save(newUser);
    System.out.println("Saved new user with ID: " + newUser.getId());

    String tokenNewUser = jwtService.generateUserToken(newUser.getId(), longExpiry);
    response.setHeader("Authorization", "Bearer " + tokenNewUser);
    System.out.println("Generated JWT token for new user");

    otpService.deleteOtp(sessionToken, "phone");
    otpService.deleteOtp(sessionToken, "email");
    System.out.println("Deleted OTPs from Redis");

    UserResponseDto userResponse = modelMapper.map(newUser, UserResponseDto.class);
    System.out.println("Mapped newUser to UserResponseDto: " + userResponse);

    System.out.println("=== END SIGNUP ===");
    return userResponse;
}



    // -------------------- MPIN -------------------- //
    public void createMpin(String mpin, HttpServletRequest request) {
        String userId = JwtService.extractUserIdFromToken(request, jwtService);
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized: Invalid or expired token");
        }

        UserEntity user = authService.getUserById(userId);
        authService.updateMpin(user, mpin);
    }

    public UserResponseDto verifyMpin(String mpin, HttpServletRequest request, HttpServletResponse response) {
        String userId = JwtService.extractUserIdFromToken(request, jwtService);
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized: Invalid or expired token");
        }

        UserEntity user = authService.getUserById(userId);

        if (!authService.verifyMpin(user, mpin)) {
            throw new UnauthorizedException("Invalid MPIN");
        }

        String accessToken = jwtService.generateUserToken(user.getId(), longExpiry);
        response.setHeader("Authorization", "Bearer " + accessToken);

        // ✅ Map UserEntity → UserResponseDto
        return modelMapper.map(user, UserResponseDto.class);
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
