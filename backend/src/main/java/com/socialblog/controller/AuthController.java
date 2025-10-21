package com.socialblog.controller;

import com.socialblog.dto.RegisterRequest;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Role;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Đăng xuất thành công!");
        }

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") RegisterRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate dữ liệu đầu vào
            if (form.getUsername() == null || form.getUsername().trim().isEmpty()) {
                model.addAttribute("errorMessage", "Tên đăng nhập không được để trống!");
                return "register";
            }

            if (form.getPassword() == null || form.getPassword().length() < 6) {
                model.addAttribute("errorMessage", "Mật khẩu phải có ít nhất 6 ký tự!");
                return "register";
            }

            // Kiểm tra xác nhận mật khẩu
            if (form.getConfirmPassword() == null || !form.getPassword().equals(form.getConfirmPassword())) {
                model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "register";
            }

            if (form.getEmail() == null || !form.getEmail().contains("@")) {
                model.addAttribute("errorMessage", "Email không hợp lệ!");
                return "register";
            }

            // Validate địa chỉ
            if (form.getAddress() == null || form.getAddress().trim().isEmpty()) {
                model.addAttribute("errorMessage", "Địa chỉ không được để trống!");
                return "register";
            }

            // Validate CCCD (12 số)
            if (form.getCitizenId() == null || !form.getCitizenId().matches("\\d{12}")) {
                model.addAttribute("errorMessage", "CCCD phải có đúng 12 số!");
                return "register";
            }

            // Kiểm tra username đã tồn tại
            if (userRepository.existsByUsername(form.getUsername())) {
                model.addAttribute("errorMessage", "Username đã tồn tại!");
                return "register";
            }

            // Kiểm tra email đã tồn tại
            if (userRepository.existsByEmail(form.getEmail())) {
                model.addAttribute("errorMessage", "Email đã tồn tại!");
                return "register";
            }

            // Tạo user mới
            User user = User.builder()
                    .username(form.getUsername().trim())
                    .fullName(form.getFullName() != null ? form.getFullName().trim() : form.getUsername())
                    .email(form.getEmail().trim().toLowerCase())
                    .phoneNumber(form.getPhoneNumber())
                    .dateOfBirth(form.getDateOfBirth())
                    .gender(form.getGender())
                    .address(form.getAddress().trim())
                    .citizenId(form.getCitizenId().trim())
                    .password(passwordEncoder.encode(form.getPassword()))
                    .active(true)
                    .verified(false)
                    .role(Role.USER)
                    .build();

            userRepository.save(user);

            log.info("Đăng ký thành công user: {}", user.getUsername());

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký: ", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra, vui lòng thử lại!");
            return "register";
        }
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }
}