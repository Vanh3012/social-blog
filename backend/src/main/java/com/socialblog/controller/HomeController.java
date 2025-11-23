package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.entity.Friendship;
import com.socialblog.repository.ReactionRepository;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.PostService;
import com.socialblog.service.FriendshipService;
import com.socialblog.service.ReactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final FriendshipService friendshipService;
    private final ReactionService reactionService;

    @GetMapping("/")
    public String home(HttpSession session, Model model,
            @RequestParam(name = "filter", defaultValue = "all") String filter) {

        UserDTO currentUserDTO = (UserDTO) session.getAttribute("currentUser");

        List<Post> posts;
        Map<Long, String> userReactions = new HashMap<>();
        List<Friendship> pendingRequests = List.of();
        List<User> friendSuggestions = List.of();
        Map<Long, List<com.socialblog.service.ReactionService.ReactionCount>> topReactions = new HashMap<>();
        List<Long> friendIds;

        if (currentUserDTO != null) {

            // User đã đăng nhập
            User currentUser = userRepository.findById(currentUserDTO.getId()).orElse(null);

            List<User> friends = friendshipService.listFriends(currentUserDTO.getId());
            friendIds = friends.stream().map(User::getId).collect(Collectors.toList());

            posts = postService.getPostsForUser(currentUser, friendIds);

            // Lấy reaction của user cho từng post
            if (currentUser != null) {
                for (Post post : posts) {
                    reactionRepository.findByPostAndUser(post, currentUser)
                            .ifPresent(reaction -> {
                                userReactions.put(post.getId(), reaction.getType().name());

                            });
                    topReactions.put(post.getId(), reactionService.topReactions(post, 3));
                }
                pendingRequests = friendshipService.listPendingReceived(currentUserDTO.getId());
                friendSuggestions = friendshipService.suggestFriends(currentUserDTO.getId(), 5);
            }

        } else {
            // Khách (chưa đăng nhập) → chỉ xem public posts
            posts = postService.getPublicPosts();
            friendIds = List.of();

        }

        final List<Long> visibleFriendIds = friendIds;
        posts = switch (filter.toLowerCase()) {
            case "public" -> posts.stream()
                    .filter(p -> p.getVisibility() == com.socialblog.model.enums.Visibility.PUBLIC)
                    .toList();
            case "friends" -> posts.stream()
                    .filter(p -> p.getVisibility() == com.socialblog.model.enums.Visibility.FRIENDS
                            && (visibleFriendIds.contains(p.getAuthor().getId())
                                    || (currentUserDTO != null
                                            && p.getAuthor().getId().equals(currentUserDTO.getId()))))
                    .toList();
            default -> posts;
        };

        // Tính top reactions cho mọi post (kể cả khách)
        for (Post p : posts) {
            topReactions.putIfAbsent(p.getId(), reactionService.topReactions(p, 3));
        }

        model.addAttribute("posts", posts);
        model.addAttribute("userReactions", userReactions);
        model.addAttribute("currentUser", currentUserDTO);
        model.addAttribute("pendingFriendRequests", pendingRequests);
        model.addAttribute("friendSuggestions", friendSuggestions);
        model.addAttribute("topReactions", topReactions);
        model.addAttribute("currentFilter", filter.toLowerCase());

        return "Post/home";
    }
}
