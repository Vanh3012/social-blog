package com.socialblog.dto;

import com.socialblog.model.enums.Visibility;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private Visibility visibility;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Author info
    private Long authorId;
    private String authorUsername;
    private String authorFullName;
    private String authorAvatarUrl;

    // User reaction status
    private String userReaction; // LIKE, LOVE, etc.
}