package com.socialblog.controller;

import com.socialblog.model.entity.Notification;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.NotificationService;
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
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // Danh sách thông báo
    @GetMapping
    public String listNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<Notification> notifications = notificationService.getUserNotifications(user);
            int unreadCount = notificationService.getUnreadCount(user);

            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("currentUser", user);

            return "notification/list"; // ← Đã sửa

        } catch (Exception e) {
            log.error("Lỗi khi xem thông báo: ", e);
            return "error";
        }
    }

    // Đánh dấu đã đọc
    @PostMapping("/{id}/read")
    public String markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            notificationService.markAsRead(id, user);

            return "redirect:/notifications";

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu đã đọc: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/notifications";
        }
    }

    // Đánh dấu tất cả đã đọc
    @PostMapping("/read-all")
    public String markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            notificationService.markAllAsRead(user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu tất cả là đã đọc!");
            return "redirect:/notifications";

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tất cả: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/notifications";
        }
    }

    // Xóa thông báo
    @PostMapping("/{id}/delete")
    public String deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            notificationService.deleteNotification(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thông báo!");
            return "redirect:/notifications";

        } catch (Exception e) {
            log.error("Lỗi khi xóa thông báo: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/notifications";
        }
    }
}
