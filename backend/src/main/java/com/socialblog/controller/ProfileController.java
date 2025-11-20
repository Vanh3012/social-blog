package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.PostImage;
import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.ReactionRepository;
import com.socialblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.socialblog.service.ProfileService;
import com.socialblog.repository.PostImageRepository;
import com.socialblog.service.FriendshipService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ProfileService profileService;
    private final PostImageRepository postImageRepository;
    private final ReactionRepository reactionRepository;
    private final FriendshipService friendshipService;

    // ====================== XEM PROFILE USER ======================
    @GetMapping("/user/{id}")
    public String viewProfile(@PathVariable Long id, HttpSession session, Model model) {

        // Người đang đăng nhập
        UserDTO currentUserDTO = (UserDTO) session.getAttribute("currentUser");

        // Thông tin profile cần xem
        User profileUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> posts;
        List<Friendship> pendingRequests = List.of();
        List<User> friendSuggestions = List.of();
        List<User> friends = List.of();
        String friendshipStatus = FriendshipStatus.NONE.name();
        Long friendshipId = null;
        boolean isRequestSender = false;

        // Nếu chủ trang vào xem → show ALL posts
        if (currentUserDTO != null && currentUserDTO.getId().equals(id)) {
            posts = postRepository.findByAuthorOrderByCreatedAtDesc(profileUser);

        } else {
            // Người khác xem → chỉ hiển thị PUBLIC
            posts = postRepository.findByAuthorAndVisibilityOrderByCreatedAtDesc(
                    profileUser, Visibility.PUBLIC);
        }

        // Lấy tất cả ảnh của các bài viết để hiển thị
        List<PostImage> allImages = new ArrayList<>();
        for (Post p : posts) {
            allImages.addAll(p.getImages());
        }

        // Lấy reaction của current user cho từng post
        Map<Long, String> userReactions = new HashMap<>();

        if (currentUserDTO != null) {
            User currentUser = userRepository.findById(currentUserDTO.getId())
                    .orElse(null);

            if (currentUser != null) {
                for (Post post : posts) {
                    reactionRepository.findByPostAndUser(post, currentUser)
                            .ifPresent(reaction -> userReactions.put(post.getId(), reaction.getType().name()));
                }

                Friendship friendship = friendshipService.findBetween(currentUserDTO.getId(), profileUser.getId())
                        .orElse(null);
                if (friendship != null) {
                    friendshipStatus = friendship.getStatus().name();
                    friendshipId = friendship.getId();
                    isRequestSender = friendship.getSender().getId().equals(currentUserDTO.getId());
                }

                pendingRequests = friendshipService.listPendingReceived(currentUserDTO.getId());
                friendSuggestions = friendshipService.suggestFriends(currentUserDTO.getId(), 5);
            }
        }
        friends = friendshipService.listFriends(profileUser.getId());
        model.addAttribute("userReactions", userReactions);
        model.addAttribute("profileUser", profileUser);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", currentUserDTO);
        model.addAttribute("images", allImages);
        model.addAttribute("friendshipStatus", friendshipStatus);
        model.addAttribute("friendshipId", friendshipId);
        model.addAttribute("isRequestSender", isRequestSender);
        model.addAttribute("pendingFriendRequests", pendingRequests);
        model.addAttribute("friendSuggestions", friendSuggestions);
        model.addAttribute("friends", friends);

        return "User/profile";
    }

    // =================== TRANG XEM TẤT CẢ ẢNH ===================
    @GetMapping("/user/{id}/photos")
    public String viewUserPhotos(@PathVariable Long id, Model model, HttpSession session) {
        User profileUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        // Lấy tất cả ảnh user này đăng
        List<PostImage> photos = postImageRepository.findAllImagesByUserId(id);

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("images", photos);
        model.addAttribute("currentUser", currentUser);

        return "User/photos";
    }

    // ============== MỞ TRANG EDIT ==============
    @GetMapping("/user/{id}/edit")
    public String editProfile(@PathVariable Long id,
            HttpSession session,
            Model model) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getId().equals(id)) {
            return "redirect:/login";
        }

        User user = userRepository.findById(id).orElseThrow();

        model.addAttribute("user", user);
        return "User/edit-profile";
    }

    // ============== UPDATE INFO ==============
    @PostMapping("/user/{id}/update")
    public String updateProfile(@PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dob,
            @RequestParam(required = false) String gender,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getId().equals(id)) {
            return "redirect:/login";
        }

        User user = userRepository.findById(id).orElseThrow();

        profileService.updateUserInfo(user, fullName, bio, address, dob, gender);

        ra.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/user/" + id;
    }

    // ============== UPDATE AVATAR ==============
    @PostMapping("/user/{id}/avatar")
    public String updateAvatar(@PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatarFile,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getId().equals(id)) {
            return "redirect:/login";
        }

        User user = userRepository.findById(id).orElseThrow();

        profileService.updateAvatar(user, avatarFile);

        ra.addFlashAttribute("success", "Đổi ảnh đại diện thành công!");
        return "redirect:/user/" + id;
    }

    // ============== UPDATE COVER ==============
    @PostMapping("/user/{id}/cover")
    public String updateCover(@PathVariable Long id,
            @RequestParam("cover") MultipartFile coverFile,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null || !currentUser.getId().equals(id)) {
            return "redirect:/login";
        }

        User user = userRepository.findById(id).orElseThrow();

        profileService.updateCover(user, coverFile);

        ra.addFlashAttribute("success", "Cập nhật ảnh bìa thành công!");
        return "redirect:/user/" + id;
    }

}
