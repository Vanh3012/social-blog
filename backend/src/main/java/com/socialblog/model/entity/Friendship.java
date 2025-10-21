package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import com.socialblog.model.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "friendships")
public class Friendship extends BaseEntity {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private FriendshipStatus status;

        // ===== Người gửi yêu cầu kết bạn =====
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_id", nullable = false)
        private User sender;

        // ===== Người nhận yêu cầu =====
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_id", nullable = false)
        private User receiver;
}
