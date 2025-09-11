package com.prp.authservice.service;

import com.prp.commonconfig.exception.UserAlreadyExistsException;
import com.prp.authservice.model.User;
import com.prp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bcryptPasswordEncoder;

    public User signup(String fullName, String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber)
                .ifPresent(u -> { throw new UserAlreadyExistsException("User already exists with this phone number"); });

        User user = User.builder()
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .verified(true)
                .build();

        return userRepository.save(user);
    }

    public User getUserByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateMpin(User user, String rawMpin) {
        user.setMpin(bcryptPasswordEncoder.encode(rawMpin));
        userRepository.save(user);
    }

    public boolean verifyMpin(User user, String rawMpin) {
        return bcryptPasswordEncoder.matches(rawMpin, user.getMpin());
    }
}
