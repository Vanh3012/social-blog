package com.socialblog.controller;

import com.socialblog.dto.PostRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.PostService;
import com.socialblog.service.ReactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.socialblog.dto.CommentRequest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final ReactionService reactionService;
    private final UserRepository userRepository;

    // HIỂN THỊ FORM TẠO BÀI
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model, RedirectAttributes ra) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để đăng bài");
            return "redirect:/auth/login";
        }

        model.addAttribute("post", new PostRequest());
        model.addAttribute("visibilities", Visibility.values());
        model.addAttribute("currentUser", currentUser);
        return "Post/create";
    }

    // XỬ LÝ SUBMIT FORM TẠO BÀI
    @PostMapping("/create")
    public String createPost(
            @ModelAttribute("post") PostRequest request,
            @RequestParam("images") MultipartFile[] images,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        UserDTO cur = (UserDTO) session.getAttribute("currentUser");
        if (cur == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập!");
            return "redirect:/auth/login";
        }

        User user = userRepository.findById(cur.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        try {
            postService.createPost(request, user, List.of(images), videos != null ? List.of(videos) : List.of());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng bài thành công!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/create";
        }
    }

    // XEM CHI TIẾT BÀI VIẾT
    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        try {
            Post post = postService.getPostById(id);
            model.addAttribute("post", post);

            UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
            model.addAttribute("currentUser", currentUser);
            String userReaction = null;

            if (currentUser != null) {
                User userEntity = userRepository.findById(currentUser.getId())
                        .orElse(null);

                if (userEntity != null) {
                    userReaction = reactionService.getUserReactionForPost(id, userEntity);
                }
            }
            model.addAttribute("topReactions", reactionService.topReactions(post, 3));
            model.addAttribute("userReaction", userReaction);
            model.addAttribute("newComment", new CommentRequest());
            return "Post/detail"; // sẽ chứa comment + reaction luôn

        } catch (Exception e) {
            log.error("Lỗi xem bài viết", e);
            ra.addFlashAttribute("error", "Không tìm thấy bài viết");

            return "redirect:/";
        }
    }

    @GetMapping("/{id}/edit")
    public String editPost(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để chỉnh sửa");
            return "redirect:/auth/login";
        }

        Post post = postService.getPostById(id);
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa bài viết này");
            return "redirect:/";
        }
        model.addAttribute("post", post);
        return "Post/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePost(@PathVariable Long id,
            @RequestParam("content") String content,
            @RequestParam("visibility") Visibility visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập");
            return "redirect:/auth/login";
        }

        postService.updatePost(id, currentUser.getId(), content, visibility, images);

        ra.addFlashAttribute("success", "Cập nhật bài viết thành công");
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập");
            return "redirect:/auth/login";
        }

        Post post = postService.getPostById(id);
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa bài viết này");
            return "redirect:/";
        }

        postService.deletePost(id, currentUser.getId());
        ra.addFlashAttribute("success", "Xóa bài viết thành công");
        return "redirect:/";
    }

    // CHIA SẺ / REPOST
    @PostMapping("/{id}/repost")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> repost(@PathVariable Long id,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session) {

        Map<String, Object> resp = new HashMap<>();
        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.put("success", false);
            resp.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(resp);
        }

        try {
            Post original = postService.getPostById(id);
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            postService.repost(original, user, note);
            resp.put("success", true);
            resp.put("message", "Chia sẻ bài viết thành công");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Lỗi repost", e);
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }
}
