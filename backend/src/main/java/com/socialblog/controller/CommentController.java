package com.socialblog.controller;

import com.socialblog.dto.CommentRequest;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    // Thêm comment
    @PostMapping("/add")
    public String addComment(
            @ModelAttribute CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            // Validate
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung bình luận không được để trống!");
                return "redirect:/post/" + request.getPostId();
            }

            commentService.createComment(request, user);

            redirectAttributes.addFlashAttribute("successMessage", "Bình luận thành công!");
            return "redirect:/post/" + request.getPostId();

        } catch (Exception e) {
            log.error("Lỗi khi tạo comment: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/" + request.getPostId();
        }
    }

    // Xóa comment
    @PostMapping("/{id}/delete")
    public String deleteComment(
            @PathVariable Long id,
            @RequestParam Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            commentService.deleteComment(id, user);

            redirectAttributes.addFlashAttribute("successMessage", "Xóa bình luận thành công!");
            return "redirect:/post/" + postId;

        } catch (Exception e) {
            log.error("Lỗi khi xóa comment: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post/" + postId;
        }
    }

    // Sửa comment
    @PostMapping("/{id}/edit")
    public String editComment(
            @PathVariable Long id,
            @RequestParam Long postId,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung không được để trống!");
                return "redirect:/post/" + postId;
            }

            commentService.updateComment(id, content, user);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bình luận thành công!");
            return "redirect:/post/" + postId;

        } catch (Exception e) {
            log.error("Lỗi khi sửa comment: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/post/" + postId;
        }
    }
}