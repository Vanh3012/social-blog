package com.socialblog.dto;

import lombok.*;

// DTO cho form đăng nhập tài khoản người dùng.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String email;
    private String username;
    private String password;
}
