package com.socialblog.service;

import com.socialblog.dto.MessageRequest;
import com.socialblog.model.entity.Conversation;
import com.socialblog.model.entity.ConversationMember;
import com.socialblog.model.entity.Message;
import com.socialblog.model.entity.User;
import com.socialblog.repository.ConversationRepository;
import com.socialblog.repository.MessageRepository;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Message sendMessage(MessageRequest request, User sender) {
        Conversation conversation;

        if (request.getConversationId() != null) {
            // Gửi tin nhắn vào conversation có sẵn
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc hội thoại"));
        } else if (request.getReceiverId() != null) {
            // Tạo conversation mới với receiver
            User receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

            // Kiểm tra đã có conversation chưa
            Optional<Conversation> existingConv = conversationRepository
                    .findPrivateConversation(sender.getId(), receiver.getId());

            if (existingConv.isPresent()) {
                conversation = existingConv.get();
            } else {
                // Tạo conversation mới
                conversation = createPrivateConversation(sender, receiver);
            }
        } else {
            throw new RuntimeException("Phải có conversationId hoặc receiverId");
        }

        Message message = Message.builder()
                .content(request.getContent())
                .sender(sender)
                .conversation(conversation)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Tạo thông báo cho các thành viên khác
        notificationService.createMessageNotification(savedMessage);

        log.info("User {} sent message in conversation {}", sender.getUsername(), conversation.getId());
        return savedMessage;
    }

    @Transactional
    protected Conversation createPrivateConversation(User user1, User user2) {
        Conversation conversation = Conversation.builder()
                .name(user1.getFullName() + " & " + user2.getFullName())
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        // Thêm members
        ConversationMember member1 = ConversationMember.builder()
                .conversation(savedConversation)
                .user(user1)
                .build();

        ConversationMember member2 = ConversationMember.builder()
                .conversation(savedConversation)
                .user(user2)
                .build();

        savedConversation.getMembers().add(member1);
        savedConversation.getMembers().add(member2);

        conversationRepository.save(savedConversation);

        log.info("Created private conversation between {} and {}",
                user1.getUsername(), user2.getUsername());
        return savedConversation;
    }

    @Transactional(readOnly = true)
    public List<Message> getConversationMessages(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc hội thoại"));

        // Kiểm tra user có trong conversation không
        boolean isMember = conversation.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new RuntimeException("Bạn không có quyền xem cuộc hội thoại này");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findByUserId(user.getId());
    }

    @Transactional
    public void deleteMessage(Long messageId, User user) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa tin nhắn này");
        }

        messageRepository.delete(message);
        log.info("User {} deleted message {}", user.getUsername(), messageId);
    }
}