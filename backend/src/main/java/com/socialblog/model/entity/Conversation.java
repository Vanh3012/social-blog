package com.socialblog.model.entity;

import com.socialblog.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "is_group")
    private boolean group = false; // false = private chat, true = group chat

    // Quan hệ 1-n với Message
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    // Nhiều người có thể tham gia 1 cuộc trò chuyện
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationMember> members = new ArrayList<>();
}