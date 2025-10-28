package com.socialblog.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private Long parentId; // Null nếu không phải reply
    private String content;
}