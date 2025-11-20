package com.socialblog.controller;

import com.socialblog.dto.CommentRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.CommentRepository;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.socialblog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    @PostMapping("/comment/add")
    public String addComment(CommentRequest request, HttpSession session) {

        UserDTO userDTO = (UserDTO) session.getAttribute("currentUser");
        if (userDTO == null) {
            return "redirect:/login";
        }

        // 2. Truy vấn User entity thật từ DB
        User author = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Lấy bài viết
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 4. Tạo comment mới
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content(request.getContent())
                .build();

        // 5. Nếu là reply
        if (request.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        // 6. Lưu comment
        commentRepository.save(comment);

        // 7. Tăng số comment trong post (tuỳ bạn muốn)
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // 8. Quay lại trang detail
        return "redirect:/post/" + request.getPostId();
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra,
            HttpServletRequest request) {
        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");

        if (currentUser == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập.");
            return "redirect:/login";
        }

        try {
            commentService.deleteComment(id, currentUser.getId());
            ra.addFlashAttribute("success", "Xóa bình luận thành công.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

}
