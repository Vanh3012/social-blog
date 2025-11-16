package com.socialblog.service;

import com.socialblog.dto.LoginRequest;
import com.socialblog.dto.RegisterRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Role;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) 
                .fullName(request.getFullName())
                .role(Role.USER)
                .active(true)
                .verified(true)
                .build();

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    public UserDTO login(LoginRequest request) {
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username hoặc password không đúng!"));

        // Kiểm tra password (so sánh trực tiếp)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Username hoặc password không đúng!");
        }

        // Kiểm tra tài khoản có active không
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa!");
        }

        return convertToDTO(user);
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        return convertToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .coverUrl(user.getCoverUrl())
                .bio(user.getBio())
                .gender(user.getGender())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}