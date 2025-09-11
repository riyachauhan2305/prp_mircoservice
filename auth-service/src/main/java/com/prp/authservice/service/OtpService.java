package com.prp.authservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.Random;
import com.prp.commonconfig.exception.InvalidOtpException;
import com.prp.authservice.service.EmailSenderService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final StringRedisTemplate redisTemplate;
    private final EmailSenderService emailSenderService;

    private static final int OTP_LENGTH = 6;
    private static final long RESEND_COOLDOWN_SECONDS = 30;
    private static final long OTP_VALIDITY_SECONDS = 600; // 10 minutes

    // -------------------- Unified Key Builders -------------------- //
    private String buildOtpKey(String sessionToken, String type) {
        return "otp:" + type + ":" + sessionToken;
    }

    private String buildVerifiedKey(String sessionToken, String type) {
        return "verified:" + type + ":" + sessionToken;
    }

    private String buildIdentifierKey(String sessionToken, String type) {
        return "otp:session:" + sessionToken + ":" + type;
    }

    private String buildAttemptKey(String sessionToken, String type) {
        return "otp:" + type + ":attempts:" + sessionToken;
    }

    private String buildResendKey(String identifier, String type) {
        return "otp:resend:" + type + ":" + identifier;
    }

    // -------------------- OTP Generation & Hash -------------------- //
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit OTP
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing OTP", e);
        }
    }

  

    public String storeOtp(String identifier, String type, String sessionToken) {
    if (sessionToken == null) {
        sessionToken = UUID.randomUUID().toString();
    }

    String resendKey = buildResendKey(identifier, type);
    if (redisTemplate.hasKey(resendKey)) {
        throw new RuntimeException("Please wait " + RESEND_COOLDOWN_SECONDS + "s before requesting another OTP");
    }

    String otp = generateOtp();
    String hashedOtp = hashOtp(otp);

    // Store OTP
    redisTemplate.opsForValue().set(buildOtpKey(sessionToken, type), hashedOtp, OTP_VALIDITY_SECONDS, TimeUnit.SECONDS);
    // Store identifier
    redisTemplate.opsForValue().set(buildIdentifierKey(sessionToken, type), identifier, OTP_VALIDITY_SECONDS, TimeUnit.SECONDS);
    // Initialize attempts
    redisTemplate.opsForValue().set(buildAttemptKey(sessionToken, type), "0", OTP_VALIDITY_SECONDS, TimeUnit.SECONDS);
    // Set resend cooldown
    redisTemplate.opsForValue().set(resendKey, "cooldown", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);

    log.info("Generated {} OTP [{}] for {}", type, otp, identifier);

    // Send OTP
    sendOtp(identifier, type, otp);

    return sessionToken;
}


    // -------------------- Send OTP -------------------- //
    private void sendOtp(String identifier, String type, String otp) {
        if ("email".equalsIgnoreCase(type)) {
            emailSenderService.sendEmail(identifier, "Your OTP is: " + otp);
            log.info("Sent EMAIL OTP to {}", identifier);
        } else if ("phone".equalsIgnoreCase(type)) {
            // Implement SMS sending here
            log.info("Sent PHONE OTP to {}", identifier);
        } else {
            throw new RuntimeException("Unknown OTP type: " + type);
        }
    }

    // -------------------- Verify OTP -------------------- //
    public void verifyOtp(String sessionToken, String otp, String type) {
        String key = buildOtpKey(sessionToken, type);
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) throw new InvalidOtpException("OTP expired or not found");
        if (!hashOtp(otp).equals(storedOtp)) throw new InvalidOtpException("OTP does not match");

        // Store verified flag
        redisTemplate.opsForValue().set(buildVerifiedKey(sessionToken, type), "true", OTP_VALIDITY_SECONDS, TimeUnit.SECONDS);
        // Remove OTP
        redisTemplate.delete(key);

        log.info("{} OTP verified successfully for session {}", type, sessionToken);
    }

    // -------------------- Check Verified -------------------- //
    public boolean isVerified(String sessionToken, String type) {
        String value = redisTemplate.opsForValue().get(buildVerifiedKey(sessionToken, type));
        return "true".equals(value);
    }

    // -------------------- Get Identifier by Session -------------------- //
    public String getIdentifierBySessionToken(String sessionToken, String type) {
        return redisTemplate.opsForValue().get(buildIdentifierKey(sessionToken, type));
    }

    // -------------------- Delete OTP -------------------- //
    public void deleteOtp(String sessionToken, String type) {
        redisTemplate.delete(buildOtpKey(sessionToken, type));
        redisTemplate.delete(buildVerifiedKey(sessionToken, type));
        redisTemplate.delete(buildIdentifierKey(sessionToken, type));
    }


    
}
