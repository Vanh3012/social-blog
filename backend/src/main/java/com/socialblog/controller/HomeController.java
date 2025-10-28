package com.socialblog.controller;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.service.NotificationService;
import com.socialblog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final PostService postService;
    private final NotificationService notificationService;
    private final com.socialblog.repository.UserRepository userRepository;

    @GetMapping("/")
    public String home(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        try {
            if (userDetails != null) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Lấy danh sách bài viết có thể xem
                List<Post> posts = postService.getVisiblePosts(user);

                // Lấy số thông báo chưa đọc
                int unreadCount = notificationService.getUnreadCount(user);

                model.addAttribute("posts", posts);
                model.addAttribute("currentUser", user);
                model.addAttribute("unreadCount", unreadCount);

                // Trang chủ sau đăng nhập
                return "Post/home";
            } else {
                // Nếu chưa đăng nhập, hiển thị bài viết public
                List<Post> posts = postService.getAllPublicPosts();
                model.addAttribute("posts", posts);
                return "Post/home";
            }
        } catch (Exception e) {
            log.error("Error loading home page: ", e);
            return "error";
        }
    }

    @GetMapping("/about")
    public String about() {
        return "static/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "static/contact";
    }
}
