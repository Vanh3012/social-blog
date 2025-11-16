package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import com.socialblog.model.enums.ReactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reactions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "post_id" }))
public class Reaction extends BaseEntity {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ReactionType type;

        // ===== Quan hệ với bài viết =====
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        private Post post;

        // ===== Quan hệ với người dùng =====
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;
}
