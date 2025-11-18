package com.socialblog.controller;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.ReactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reaction")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {

    private final ReactionService reactionService;
    private final UserRepository userRepository;

    /**
     * Thêm hoặc đổi reaction
     * POST /reaction/add
     * Body: { "postId": 1, "type": "LIKE" }
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addReaction(
            @RequestBody ReactionRequest request,
            HttpSession session) {

        log.info("📝 Add reaction request - postId: {}, type: {}", request.getPostId(), request.getType());

        try {
            // Lấy user từ session
            User user = getUserFromSession(session);

            // Gọi service để thêm/đổi reaction
            long totalReactions = reactionService.addOrUpdateReaction(request, user);

            // Trả về response thành công
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalReactions", totalReactions);
            response.put("reactionType", request.getType().name());
            response.put("message", "Reaction added successfully");

            log.info("✅ Reaction added - Total: {}", totalReactions);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ Error adding reaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Xóa reaction
     * DELETE /reaction/remove/{postId}
     */
    @DeleteMapping("/remove/{postId}")
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long postId,
            HttpSession session) {

        log.info("🗑️ Remove reaction request - postId: {}", postId);

        try {
            // Lấy user từ session
            User user = getUserFromSession(session);

            // Gọi service để xóa reaction
            long totalReactions = reactionService.removeReaction(postId, user);

            // Trả về response thành công
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalReactions", totalReactions);
            response.put("message", "Reaction removed successfully");

            log.info("✅ Reaction removed - Total: {}", totalReactions);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("❌ Error removing reaction: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Lấy User từ session
     */
    private User getUserFromSession(HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("currentUser");

        if (userDTO == null) {
            throw new RuntimeException("Bạn cần đăng nhập để thực hiện hành động này!");
        }

        return userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));
    }

    /**
     * Tạo error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}