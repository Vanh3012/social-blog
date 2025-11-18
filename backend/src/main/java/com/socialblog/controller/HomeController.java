package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.repository.ReactionRepository;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {

        UserDTO currentUserDTO = (UserDTO) session.getAttribute("currentUser");

        List<Post> posts;
        Map<Long, String> userReactions = new HashMap<>();

        if (currentUserDTO != null) {

            // User đã đăng nhập
            User currentUser = userRepository.findById(currentUserDTO.getId()).orElse(null);

            posts = postService.getPostsForUser(currentUser);

            // Lấy reaction của user cho từng post
            if (currentUser != null) {
                for (Post post : posts) {
                    reactionRepository.findByPostAndUser(post, currentUser)
                            .ifPresent(reaction -> {
                                userReactions.put(post.getId(), reaction.getType().name());

                            });
                }
            }

        } else {
            // Khách (chưa đăng nhập) → chỉ xem public posts
            posts = postService.getPublicPosts();

        }

        model.addAttribute("posts", posts);
        model.addAttribute("userReactions", userReactions);
        model.addAttribute("currentUser", currentUserDTO);

        return "Post/home";
    }
}