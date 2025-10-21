package com.socialblog.dto;

import com.socialblog.model.enums.Gender;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String username;
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
    private String address;
    private String citizenId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private Gender gender;
}