package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
public class Message extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    // Tin nhắn đã đọc hay chưa
    @Column(name = "is_read")
    private boolean isRead = false;

    // ===== Quan hệ với người gửi =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // ===== Quan hệ với cuộc hội thoại =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
}
