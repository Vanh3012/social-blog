package com.socialblog.controller;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.FriendshipService;
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
@RequestMapping("/friend")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserRepository userRepository;

    // Danh sách bạn bè
    @GetMapping("/list")
    public String friendList(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<User> friends = friendshipService.getFriends(user);
            List<Friendship> pendingRequests = friendshipService.getPendingRequests(user);

            model.addAttribute("friends", friends);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("currentUser", user);

            return "User/friend";

        } catch (Exception e) {
            log.error("Lỗi khi xem danh sách bạn bè: ", e);
            return "error";
        }
    }

    // Tìm kiếm người dùng
    @GetMapping("/search")
    public String searchUsers(
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            List<User> users = null;

            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userRepository.findAll().stream()
                        .filter(u -> !u.getId().equals(currentUser.getId()))
                        .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase())
                                || (u.getFullName() != null
                                        && u.getFullName().toLowerCase().contains(keyword.toLowerCase())))
                        .toList();
            }

            model.addAttribute("users", users);
            model.addAttribute("keyword", keyword);
            model.addAttribute("currentUser", currentUser);

            return "User/search";

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm: ", e);
            return "error";
        }
    }

    // Gửi lời mời kết bạn
    @PostMapping("/request/{id}")
    public String sendFriendRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            friendshipService.sendFriendRequest(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi lời mời kết bạn!");
            return "redirect:/friend/search";

        } catch (Exception e) {
            log.error("Lỗi khi gửi lời mời: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/friend/search";
        }
    }

    // Chấp nhận lời mời kết bạn
    @PostMapping("/accept/{id}")
    public String acceptFriendRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            friendshipService.acceptFriendRequest(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã chấp nhận lời mời kết bạn!");
            return "redirect:/friend/list";

        } catch (Exception e) {
            log.error("Lỗi khi chấp nhận lời mời: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/friend/list";
        }
    }

    // Từ chối lời mời kết bạn
    @PostMapping("/decline/{id}")
    public String declineFriendRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            friendshipService.declineFriendRequest(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối lời mời kết bạn!");
            return "redirect:/friend/list";

        } catch (Exception e) {
            log.error("Lỗi khi từ chối lời mời: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/friend/list";
        }
    }

    // Hủy kết bạn
    @PostMapping("/unfriend/{id}")
    public String unfriend(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            friendshipService.unfriend(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy kết bạn!");
            return "redirect:/friend/list";

        } catch (Exception e) {
            log.error("Lỗi khi hủy kết bạn: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/friend/list";
        }
    }

    // Xem profile người dùng khác
    @GetMapping("/profile/{id}")
    public String viewProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            User profileUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            FriendshipStatus status = friendshipService.getFriendshipStatus(currentUser, profileUser);
            boolean areFriends = friendshipService.areFriends(currentUser.getId(), profileUser.getId());

            model.addAttribute("profileUser", profileUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("friendshipStatus", status);
            model.addAttribute("areFriends", areFriends);

            return "User/friend";

        } catch (Exception e) {
            log.error("Lỗi khi xem profile: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/friend/search";
        }
    }
}