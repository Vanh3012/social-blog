package com.socialblog.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long conversationId;
    private Long receiverId; // Dùng khi tạo conversation mới
    private String content;
}