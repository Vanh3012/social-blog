package com.socialblog.dto;

import com.socialblog.model.enums.Gender;
import lombok.*;
import java.time.LocalDate;

// DTO cho form đăng ký tài khoản người dùng.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;

    private String fullName;

    private String email;

    private String password;

    private String confirmPassword; // để xác nhận khi đăng ký

    private String phoneNumber;

    private String address;

    private String citizenId; // CCCD (12 số)

    private LocalDate dateOfBirth;

    private Gender gender; // enum: MALE, FEMALE, OTHER
}
