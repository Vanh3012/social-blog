package com.socialblog.dto;

import com.socialblog.model.enums.Gender;
import com.socialblog.model.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String coverUrl;
    private String bio;
    private Gender gender;
    private Role role;
    private LocalDateTime createdAt;
}