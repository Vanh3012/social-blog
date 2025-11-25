package com.socialblog.controller;

import com.socialblog.model.entity.ConversationMember;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Conversation;
import com.socialblog.model.entity.Message;
import com.socialblog.model.entity.User;
import com.socialblog.service.ConversationService;
import com.socialblog.service.MessageService;
import com.socialblog.repository.MessageRepository;
import com.socialblog.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // ======================= Mở chat =======================
    @GetMapping("/user/{partnerId}")
    public String openChatWithUser(@PathVariable Long partnerId,
            HttpSession session,
            Model model) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Long currentUserId = currentUser.getId();

        Conversation conversation = conversationService.getOrCreatePrivateConversation(currentUserId, partnerId);

        List<Message> messages = messageService.getMessagesByConversation(conversation.getId());
        messageService.markMessagesAsRead(conversation.getId(), currentUserId);

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("partner", partner);

        return "Message/chat";
    }

    // ======================= Gửi tin nhắn =======================
    @PostMapping("/send")
    public String sendMessage(@RequestParam Long conversationId,
            @RequestParam Long receiverId,
            @RequestParam String content,
            HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        messageService.sendMessage(
                currentUser.getId(),
                receiverId,
                conversationId,
                content);

        return "redirect:/messages/user/" + receiverId;
    }

    // ======================= LẤY DANH SÁCH CHAT =======================
    @GetMapping("/list")
    @ResponseBody
    public Object getConversations(HttpSession session) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return List.of();
        }

        Long userId = currentUser.getId();

        return conversationService.getUserConversations(userId)
                .stream()

                // 🚀 Sắp xếp để cuộc trò chuyện mới nhất nằm trên cùng
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))

                .map(conv -> {

                    Message lastMsg = conv.getMessages().isEmpty()
                            ? null
                            : conv.getMessages().get(conv.getMessages().size() - 1);

                    ConversationMember partnerMember = conv.getMembers()
                            .stream()
                            .filter(m -> !m.getUser().getId().equals(userId))
                            .findFirst()
                            .orElse(null);

                    User partner = partnerMember != null ? partnerMember.getUser() : null;

                    long unread = conv.getMessages()
                            .stream()
                            .filter(m -> !m.isRead() && !m.getSender().getId().equals(userId))
                            .count();

                    String avatar = null;
                    if (partner != null) {
                        String raw = partner.getAvatarUrl();
                        if (raw != null && !raw.isBlank()) {
                            avatar = raw.startsWith("http")
                                    ? raw
                                    : (raw.contains("/uploads_avatar/")
                                            ? raw
                                            : "/uploads_avatar/" + raw);
                        }
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("conversationId", conv.getId());
                    data.put("partnerId", partner != null ? partner.getId() : null);
                    data.put("partnerName", partner != null ? partner.getFullName() : "Unknown");
                    data.put("partnerAvatar", avatar);
                    data.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
                    data.put("unread", unread);
                    data.put("updatedAt", conv.getUpdatedAt().toString());

                    return data;
                })
                .toList();
    }

}
