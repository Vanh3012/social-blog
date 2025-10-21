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
@Table(name = "conversation_members")
public class ConversationMember extends BaseEntity {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "conversation_id", nullable = false)
        private Conversation conversation;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;
}
