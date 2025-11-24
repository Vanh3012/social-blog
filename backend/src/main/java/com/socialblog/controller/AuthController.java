package com.socialblog.controller;

import com.socialblog.dto.LoginRequest;
import com.socialblog.dto.RegisterRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.socialblog.model.enums.Role;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Hiển thị trang đăng ký
    @GetMapping("/register")
    public String showRegisterPage() {
        return "Auth/register";
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "Auth/login";
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = authService.login(request);

            // Lưu thông tin user vào session
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());

            if (user.getRole() == Role.ADMIN) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/auth/login";
    }

    // Check authentication (helper method for other controllers)
    public static Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    public static UserDTO getCurrentUser(HttpSession session) {
        return (UserDTO) session.getAttribute("currentUser");
    }
}
