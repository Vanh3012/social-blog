package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @GetMapping("/user/{id}")
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {

        // Người đang đăng nhập
        UserDTO currentUserDTO = (UserDTO) session.getAttribute("currentUser");

        // Thông tin profile cần xem
        User profileUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> posts;

        // Nếu chủ trang vào xem → show ALL posts
        if (currentUserDTO != null && currentUserDTO.getId().equals(id)) {
            posts = postRepository.findByAuthorOrderByCreatedAtDesc(profileUser);
        } else {
            // Người khác xem → chỉ hiển thị PUBLIC
            posts = postRepository.findByAuthorAndVisibilityOrderByCreatedAtDesc(
                    profileUser, Visibility.PUBLIC);
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", currentUserDTO);

        return "User/profile";
    }
}
