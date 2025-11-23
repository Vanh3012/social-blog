package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            HttpSession session,
            Model model) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        try {
            if (from != null && !from.isBlank()) {
                fromDate = LocalDate.parse(from).atStartOfDay();
            }
            if (to != null && !to.isBlank()) {
                toDate = LocalDate.parse(to).atTime(LocalTime.MAX);
            }
        } catch (Exception ignored) {
        }

        List<Post> posts = List.of();
        List<User> users = List.of();

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        if (!"users".equalsIgnoreCase(type)) {
            posts = postRepository.searchPosts(kw, fromDate, toDate);
        }
        if (!"posts".equalsIgnoreCase(type)) {
            users = kw == null ? List.of() : userRepository.findByFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(kw, kw);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("users", users);
        model.addAttribute("q", keyword);
        model.addAttribute("type", type.toLowerCase());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("currentUser", currentUser);

        return "Post/find";
    }
}
