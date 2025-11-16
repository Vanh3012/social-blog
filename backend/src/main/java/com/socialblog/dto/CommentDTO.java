package com.socialblog.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Author info
    private Long authorId;
    private String authorUsername;
    private String authorFullName;
    private String authorAvatarUrl;

    // Post info
    private Long postId;

    // Parent comment (for replies)
    private Long parentCommentId;

    // Replies
    private List<CommentDTO> replies;
}