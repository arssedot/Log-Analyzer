package com.arssedot.loganalyzer.service;

import com.arssedot.loganalyzer.domain.User;
import com.arssedot.loganalyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role("USER")
                .enabled(true)
                .build();
        return userRepository.save(user);
    }
}
