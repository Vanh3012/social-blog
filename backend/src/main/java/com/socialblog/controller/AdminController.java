package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.User;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Report;
import com.socialblog.repository.UserRepository;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.ReportRepository;
import com.socialblog.repository.CommentRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;

    // ===================== DASHBOARD =====================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes ra) {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long totalComments = commentRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long pendingReports = reportRepository.countByStatusPending();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("totalComments", totalComments);
        stats.put("activeUsers", activeUsers);
        stats.put("pendingReports", pendingReports);

        model.addAttribute("stats", stats);
        return "Admin/dashboard";
    }

    // ===================== QUẢN LÝ NGƯỜI DÙNG =====================
    @GetMapping("/users")
    public String listUsers(HttpSession session, Model model, RedirectAttributes ra) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "Admin/users";
    }

    @PostMapping("/users/{id}/lock")
    public String lockUser(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setActive(false);
        userRepository.save(user);
        ra.addFlashAttribute("success", "Đã khoá tài khoản người dùng!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setActive(true);
        userRepository.save(user);
        ra.addFlashAttribute("success", "Đã mở tài khoản người dùng!");
        return "redirect:/admin/users";
    }

    // ===================== QUẢN LÝ BÀI VIẾT =====================
    @GetMapping("/posts")
    public String listPosts(HttpSession session, Model model, RedirectAttributes ra) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "Admin/posts";
    }

    // ✅ THÊM ENDPOINT NÀY - Xem chi tiết bài viết
    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        model.addAttribute("post", post);
        return "Admin/post-detail";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpSession session,
            RedirectAttributes ra) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        log.info("Xoá bài viết {}: {}", id, reason);
        postRepository.delete(post);
        ra.addFlashAttribute("success", "Đã xoá bài viết!");
        return "redirect:/admin/posts";
    }

    // ===================== QUẢN LÝ BÁO CÁO =====================
    @GetMapping("/reports")
    public String listReports(HttpSession session, Model model, RedirectAttributes ra) {
        List<Report> reports = reportRepository.findAll();
        model.addAttribute("reports", reports);
        return "Admin/reports";
    }

    @GetMapping("/reports/{id}")
    public String viewReport(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

        model.addAttribute("report", report);
        return "Admin/report-detail";
    }

    @PostMapping("/reports/{id}/process")
    public String processReport(@PathVariable Long id,
            @RequestParam String action,
            @RequestParam String notes,
            HttpSession session,
            RedirectAttributes ra) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

        UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
        User admin = userRepository.findById(currentUser.getId()).orElseThrow();

        report.setAdmin(admin);
        report.setAdminNotes(notes);

        if ("approve".equals(action)) {
            report.setStatus(com.socialblog.model.enums.ReportStatus.RESOLVED);
            log.info("Phê duyệt báo cáo {}", id);
        } else {
            report.setStatus(com.socialblog.model.enums.ReportStatus.REJECTED);
            log.info("Từ chối báo cáo {}", id);
        }

        reportRepository.save(report);
        ra.addFlashAttribute("success", "Đã xử lý báo cáo!");
        return "redirect:/admin/reports";
    }
}