package com.prp.authservice.controller;

import com.prp.authservice.model.ApiResponse;
import com.prp.authservice.model.User;
import com.prp.authservice.service.AuthFlowService;
import com.prp.authservice.service.OtpService;
import com.prp.authservice.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFlowService authFlowService;
    private final JwtService jwtService;
    private final OtpService otpService;

    // -------------------- HEALTH CHECK -------------------- //
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    // -------------------- REQUEST PHONE OTP -------------------- //
    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@Valid @RequestBody User user,
                                                  HttpServletResponse response) {

        // sessionToken is returned from service
        String sessionToken = authFlowService.requestOtp(user.getPhoneNumber(), response);

        Map<String, String> data = new HashMap<>();
        data.put("sessionToken", sessionToken);

        return ResponseEntity.accepted()
                .body(new ApiResponse(202, "OTP sent to your phone number", data));
    }

    // -------------------- VERIFY PHONE OTP -------------------- //
    @PostMapping("/verify-phone-otp")
    public ResponseEntity<ApiResponse> verifyPhoneOtp(@Valid @RequestBody User user,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        log.info("Processing verify-phone-otp for phone: {}", user.getPhoneNumber());

        String jwtToken = authFlowService.verifyPhoneOtp(user.getOtp(), request);

        // JWT in header
        response.setHeader("Authorization", "Bearer " + jwtToken);

        Map<String, String> data = new HashMap<>();
        data.put("accessToken", jwtToken);

        return ResponseEntity.ok(new ApiResponse(200, "Phone OTP verified successfully", data));
    }

    // -------------------- SEND EMAIL OTP -------------------- //
    @PostMapping("/send-email-verification")
    public ResponseEntity<ApiResponse> sendEmailVerification(@RequestBody User user,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {

        String sessionToken = authFlowService.sendEmailVerification(user.getEmail(), request, response);

        Map<String, String> data = new HashMap<>();
        data.put("sessionToken", sessionToken);

        return ResponseEntity.ok(new ApiResponse(200, "Email OTP sent successfully", data));
    }

    // -------------------- VERIFY EMAIL OTP -------------------- //
    @PostMapping("/verify-email-otp")
    public ResponseEntity<ApiResponse> verifyEmailOtp(@RequestBody User user,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        String jwtToken = authFlowService.verifyEmailOtp(user.getOtp(), request);

        // JWT in header
        response.setHeader("Authorization", "Bearer " + jwtToken);

        Map<String, String> data = new HashMap<>();
        data.put("accessToken", jwtToken);

        return ResponseEntity.ok(new ApiResponse(200, "Email OTP verified successfully", data));
    }

    // -------------------- SIGNUP -------------------- //
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody User user,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {

        User newUser = authFlowService.signup(user, request, response);

        return ResponseEntity.status(201)
                .body(new ApiResponse(201, "User created successfully", newUser));
    }

    // -------------------- MPIN -------------------- //
    @PostMapping("/create-mpin")
    public ResponseEntity<ApiResponse> createMpin(@Valid @RequestBody User user,
                                                  HttpServletRequest request) {

        authFlowService.createMpin(user.getMpin(), request);

        return ResponseEntity.ok(new ApiResponse(200, "MPIN created successfully", null));
    }

    @PostMapping("/verify-mpin")
    public ResponseEntity<ApiResponse> verifyMpin(@Valid @RequestBody User user,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        User verifiedUser = authFlowService.verifyMpin(user.getMpin(), request, response);

        return ResponseEntity.ok(new ApiResponse(200, "MPIN verified successfully", verifiedUser));
    }

    // -------------------- LOGOUT -------------------- //
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        authFlowService.logout(request);

        return ResponseEntity.ok(new ApiResponse(200, "Logged out successfully", null));
    }
}
