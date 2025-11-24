package com.socialblog.service;

import com.socialblog.model.entity.Conversation;
import com.socialblog.model.entity.Message;
import com.socialblog.model.entity.User;
import com.socialblog.repository.ConversationRepository;
import com.socialblog.repository.MessageRepository;
import com.socialblog.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public List<Message> getMessagesByConversation(Long conversationId) {
        return messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        messageRepository.markMessagesAsRead(conversationId, userId);
    }

    // Gửi tin nhắn
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, Long conversationId, String content) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message msg = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();

        Message saved = messageRepository.save(msg);

        // CẬP NHẬT UPDATED_AT ĐỂ SORT LÊN ĐẦU
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return saved;
    }
}
