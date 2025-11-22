package com.socialblog.config;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();

        // Các URL không cần đăng nhập
        if (uri.startsWith("/auth/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/images/") ||
                uri.startsWith("/static/")) {
            return true;
        }

        // Kiểm tra session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            // Chưa đăng nhập -> redirect về login
            response.sendRedirect("/auth/login");
            return false;
        }

        // Ktra quyền admin
        if (uri.startsWith("/admin/")) {
            UserDTO currentUser = (UserDTO) session.getAttribute("currentUser");
            if (currentUser == null) {
                response.sendRedirect("/auth/login");
                return false;
            }

            User userEntity = userRepository.findById(currentUser.getId()).orElse(null);
            if (userEntity == null || !userEntity.isAdmin()) {
                // Không phải admin -> redirect về home
                response.sendRedirect("/");
                return false;
            }
        }

        return true;
    }
}