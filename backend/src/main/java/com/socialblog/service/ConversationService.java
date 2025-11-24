package com.socialblog.service;

import com.socialblog.model.entity.Conversation;
import com.socialblog.model.entity.ConversationMember;
import com.socialblog.model.entity.User;
import com.socialblog.repository.ConversationMemberRepository;
import com.socialblog.repository.ConversationRepository;
import com.socialblog.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    // Tạo hoặc lấy cuộc trò chuyện riêng
    public Conversation getOrCreatePrivateConversation(Long userAId, Long userBId) {

        List<Conversation> existing = conversationRepository.findPrivateConversationBetween(userAId, userBId);

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // Tạo mới conversation
        Conversation conversation = Conversation.builder()
                .name("Private Chat")
                .group(false)
                .build();

        conversationRepository.save(conversation);

        // Thêm member
        User userA = userRepository.findById(userAId).orElseThrow();
        User userB = userRepository.findById(userBId).orElseThrow();

        conversationMemberRepository.save(
                ConversationMember.builder()
                        .conversation(conversation)
                        .user(userA)
                        .build());

        conversationMemberRepository.save(
                ConversationMember.builder()
                        .conversation(conversation)
                        .user(userB)
                        .build());

        return conversation;
    }

    // Lấy danh sách conversation của user → LUÔN SẮP XẾP ĐÚNG
    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findAllByUserIdOrderByUpdated(userId);
    }
}