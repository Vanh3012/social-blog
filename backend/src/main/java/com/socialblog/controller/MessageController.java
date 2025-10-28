package com.socialblog.controller;

import com.socialblog.dto.MessageRequest;
import com.socialblog.model.entity.Conversation;
import com.socialblog.model.entity.Message;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    // Danh sách cuộc hội thoại
    @GetMapping("/conversations")
    public String listConversations(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<Conversation> conversations = messageService.getUserConversations(user);

            model.addAttribute("conversations", conversations);
            model.addAttribute("currentUser", user);

            return "message/conversations";

        } catch (Exception e) {
            log.error("Lỗi khi xem danh sách cuộc hội thoại: ", e);
            return "error";
        }
    }

    // Xem chi tiết cuộc hội thoại
    @GetMapping("/conversation/{id}")
    public String viewConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<Message> messages = messageService.getConversationMessages(id, user);

            model.addAttribute("conversationId", id);
            model.addAttribute("messages", messages);
            model.addAttribute("currentUser", user);

            return "message/conversation";

        } catch (Exception e) {
            log.error("Lỗi khi xem cuộc hội thoại: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/message/conversations";
        }
    }

    // Gửi tin nhắn
    @PostMapping("/send")
    public String sendMessage(
            @ModelAttribute MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Validate
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung tin nhắn không được để trống!");
                if (request.getConversationId() != null) {
                    return "redirect:/message/conversation/" + request.getConversationId();
                }
                return "redirect:/message/conversations";
            }

            Message message = messageService.sendMessage(request, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi tin nhắn!");
            return "redirect:/message/conversation/" + message.getConversation().getId();

        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            if (request.getConversationId() != null) {
                return "redirect:/message/conversation/" + request.getConversationId();
            }
            return "redirect:/message/conversations";
        }
    }

    // Bắt đầu hội thoại mới với người dùng
    @GetMapping("/start/{userId}")
    public String startConversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            User receiver = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Tạo tin nhắn đầu tiên
            MessageRequest request = new MessageRequest();
            request.setReceiverId(userId);
            request.setContent("Xin chào!");

            Message message = messageService.sendMessage(request, currentUser);

            return "redirect:/message/conversation/" + message.getConversation().getId();

        } catch (Exception e) {
            log.error("Lỗi khi bắt đầu cuộc hội thoại: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/friend/list";
        }
    }

    // Xóa tin nhắn
    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable Long id,
            @RequestParam Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            messageService.deleteMessage(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tin nhắn!");
            return "redirect:/message/conversation/" + conversationId;

        } catch (Exception e) {
            log.error("Lỗi khi xóa tin nhắn: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/message/conversation/" + conversationId;
        }
    }
}
