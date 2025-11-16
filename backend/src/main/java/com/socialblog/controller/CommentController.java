package com.socialblog.controller;

import com.socialblog.dto.CommentRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PostRepository postRepository;

    @PostMapping("/add")
    public String addComment(
            @ModelAttribute CommentRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long postId = request.getPostId();

        try {
            UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập trước.");
                return "redirect:/auth/login";
            }

            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung bình luận không được để trống!");
                return "redirect:/post/" + postId;
            }

            commentService.createComment(request, user);
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận thành công!");
            return "redirect:/post/" + postId;

        } catch (Exception e) {
            log.error("Lỗi khi tạo comment: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/" + postId;
        }
    }

    // Xóa comment
    @PostMapping("/{id}/delete")
    public String deleteComment(@PathVariable Long id,
            @RequestParam Long postId,
            HttpSession session,
            RedirectAttributes ra) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập");
            return "redirect:/auth/login";
        }

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            commentService.deleteComment(id, user);

            ra.addFlashAttribute("success", "Xóa bình luận thành công");
        } catch (Exception e) {
            log.error("Lỗi xóa comment", e);
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/post/" + postId;
    }
}
