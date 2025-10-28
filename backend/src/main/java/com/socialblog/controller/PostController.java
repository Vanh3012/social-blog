package com.socialblog.controller;

import com.socialblog.dto.PostRequest;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.CommentService;
import com.socialblog.service.PostService;
import com.socialblog.service.ReactionService;
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
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final UserRepository userRepository;

    // Hiển thị form tạo bài viết
    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new PostRequest());
        model.addAttribute("visibilities", Visibility.values());
        return "post/create";
    }

    // Xử lý tạo bài viết
    @PostMapping("/create")
    public String createPost(
            @ModelAttribute("post") PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Validate
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung không được để trống!");
                return "redirect:/post/create";
            }

            postService.createPost(request, user);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo bài viết thành công!");
            return "redirect:/";

        } catch (Exception e) {
            log.error("Lỗi khi tạo bài viết: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/create";
        }
    }

    // Xem chi tiết bài viết
    @GetMapping("/{id}")
    public String viewPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Post post = postService.getPostById(id);
            List<Comment> comments = commentService.getCommentsByPost(id);

            if (userDetails != null) {
                User currentUser = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

                model.addAttribute("currentUser", currentUser);
                model.addAttribute("hasReacted", reactionService.hasUserReacted(id, currentUser));
            }

            model.addAttribute("post", post);
            model.addAttribute("comments", comments);

            return "post/detail"; // ✅ sửa

        } catch (Exception e) {
            log.error("Lỗi khi xem bài viết: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài viết!");
            return "redirect:/";
        }
    }

    // Xóa bài viết
    @PostMapping("/{id}/delete")
    public String deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            postService.deletePost(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài viết thành công!");
            return "redirect:/";

        } catch (Exception e) {
            log.error("Lỗi khi xóa bài viết: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post/" + id;
        }
    }

    // Sửa bài viết - Form
    @GetMapping("/{id}/edit")
    public String editPostForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Post post = postService.getPostById(id);

            if (!post.getAuthor().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền sửa bài viết này!");
                return "redirect:/post/" + id;
            }

            PostRequest postRequest = new PostRequest();
            postRequest.setContent(post.getContent());
            postRequest.setImageUrl(post.getImageUrl());
            postRequest.setVisibility(post.getVisibility());

            model.addAttribute("post", postRequest);
            model.addAttribute("postId", id);
            model.addAttribute("visibilities", Visibility.values());

            return "post/edit"; // ✅ sửa

        } catch (Exception e) {
            log.error("Lỗi khi mở form sửa: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/";
        }
    }

    // Sửa bài viết - Submit
    @PostMapping("/{id}/edit")
    public String editPost(
            @PathVariable Long id,
            @ModelAttribute("post") PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            postService.updatePost(id, request, user);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bài viết thành công!");
            return "redirect:/post/" + id;

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật bài viết: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post/" + id + "/edit";
        }
    }
}
