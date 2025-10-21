package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import com.socialblog.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "is_read")
    private boolean isRead = false;

    // ===== Liên kết tới User tạo thông báo (người gửi) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== Liên kết tới User nhận thông báo =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // ===== Liên kết tới bài viết =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // ===== Liên kết tới bình luận =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
