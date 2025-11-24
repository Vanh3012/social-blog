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

    // ======================= Má»ž CHAT =======================
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

    // ======================= Gá»¬I TIN NHáº®N =======================
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

    // ======================= Láº¤Y DANH SÃCH CHAT =======================
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
                .map(conv -> {

                    // ---- Láº¥y partner ----
                    ConversationMember partnerMember = conv.getMembers()
                            .stream()
                            .filter(m -> !m.getUser().getId().equals(userId))
                            .findFirst()
                            .orElse(null);

                    User partner = partnerMember != null ? partnerMember.getUser() : null;

                    // ---- Láº¥y tin nháº¯n cuá»‘i ----
                    Message lastMsg = conv.getMessages().isEmpty()
                            ? null
                            : conv.getMessages().get(conv.getMessages().size() - 1);

                    return Map.of(
                            "conversationId", conv.getId(),
                            "partnerId", partner != null ? partner.getId() : null,
                            "partnerName", partner != null ? partner.getFullName() : "Unknown",
                            "partnerAvatar", partner != null ? partner.getAvatarUrl() : null,
                            "lastMessage", lastMsg != null ? lastMsg.getContent() : "",
                            "unread", messageRepository.countByConversation_IdAndIsReadFalseAndSender_IdNot(conv.getId(), userId),
                            "updatedAt", conv.getUpdatedAt().toString());
                })
                .toList();
    }

}

