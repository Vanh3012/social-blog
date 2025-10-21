package com.socialblog.service;

import com.socialblog.dto.RegisterRequest;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean register(RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()
                || userRepository.findByUsername(req.getUsername()).isPresent()) {
            return false;
        }

        User user = User.builder()
                .username(req.getUsername())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(user);
        return true;
    }
}
