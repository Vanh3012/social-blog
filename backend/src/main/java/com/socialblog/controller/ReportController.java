package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Report;
import com.socialblog.model.entity.User;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.enums.ReportType;
import com.socialblog.model.enums.ReportStatus;
import com.socialblog.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createReport(
            @RequestParam ReportType type,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long commentId,
            HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(error("Bạn cần đăng nhập!"));
        }

        User reporter = userRepository.findById(currentUser.getId()).orElseThrow();

        Report report = Report.builder()
                .title(title)
                .description(description)
                .type(type)
                .status(ReportStatus.PENDING)
                .reporter(reporter)
                .build();

        if (ReportType.POST.equals(type) && postId != null) {
            report.setPost(postRepository.findById(postId).orElse(null));
        } else if (ReportType.USER.equals(type) && userId != null) {
            report.setReportedUser(userRepository.findById(userId).orElse(null));
        } else if (ReportType.COMMENT.equals(type) && commentId != null) {
            report.setComment(commentRepository.findById(commentId).orElse(null));
        }

        reportRepository.save(report);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Báo cáo đã được gửi!");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/post/{postId}/report")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @RequestParam String reason,
            HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(error("Bạn cần đăng nhập!"));
        }

        User reporter = userRepository.findById(currentUser.getId()).orElseThrow();
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            return ResponseEntity.badRequest().body(error("Bài viết không tồn tại!"));
        }

        Report report = Report.builder()
                .title(reason)
                .description(reason)
                .type(ReportType.POST)
                .status(ReportStatus.PENDING)
                .reporter(reporter)
                .post(post)
                .build();

        reportRepository.save(report);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Báo cáo bài viết đã được gửi!"));
    }

    @PostMapping("/user/{userId}/report")
    public ResponseEntity<?> reportUser(
            @PathVariable Long userId,
            @RequestParam String reason,
            HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(error("Bạn cần đăng nhập!"));
        }

        User reporter = userRepository.findById(currentUser.getId()).orElseThrow();
        User reportedUser = userRepository.findById(userId).orElse(null);

        if (reportedUser == null) {
            return ResponseEntity.badRequest().body(error("Người dùng không tồn tại!"));
        }

        Report report = Report.builder()
                .title(reason)
                .description(reason)
                .type(ReportType.USER)
                .status(ReportStatus.PENDING)
                .reporter(reporter)
                .reportedUser(reportedUser)
                .build();

        reportRepository.save(report);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Báo cáo người dùng đã được gửi!"));
    }

    @PostMapping("/comment/{commentId}/report")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @RequestParam String reason,
            HttpSession session) {

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(error("Bạn cần đăng nhập!"));
        }

        User reporter = userRepository.findById(currentUser.getId()).orElseThrow();
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (comment == null) {
            return ResponseEntity.badRequest().body(error("Bình luận không tồn tại!"));
        }

        Report report = Report.builder()
                .title(reason)
                .description(reason)
                .type(ReportType.COMMENT)
                .status(ReportStatus.PENDING)
                .reporter(reporter)
                .comment(comment)
                .build();

        reportRepository.save(report);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Báo cáo bình luận đã được gửi!"));
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", msg);
        return res;
    }
}