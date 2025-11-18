package com.socialblog.dto;

import lombok.Data;

@Data
public class CommentRequest {

    private Long postId;
    private Long parentCommentId;
    private String content;
}
