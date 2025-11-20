package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.FriendshipService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserRepository userRepository;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<Map<String, Object>> sendRequest(@PathVariable Long receiverId, HttpSession session) {
        try {
            Long currentUserId = requireUserId(session);
            Friendship friendship = friendshipService.sendRequest(currentUserId, receiverId);
            return ResponseEntity.ok(success(friendship, "Đã gửi lời mời kết bạn"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> accept(@PathVariable Long id, HttpSession session) {
        try {
            Long currentUserId = requireUserId(session);
            Friendship friendship = friendshipService.accept(id, currentUserId);
            return ResponseEntity.ok(success(friendship, "Đã chấp nhận lời mời"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<Map<String, Object>> decline(@PathVariable Long id, HttpSession session) {
        try {
            Long currentUserId = requireUserId(session);
            Friendship friendship = friendshipService.decline(id, currentUserId);
            return ResponseEntity.ok(success(friendship, "Đã từ chối lời mời"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long id, HttpSession session) {
        try {
            Long currentUserId = requireUserId(session);
            friendshipService.cancel(id, currentUserId);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Đã hủy lời mời");
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/status/{otherUserId}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable Long otherUserId, HttpSession session) {
        try {
            Long currentUserId = requireUserId(session);
            Optional<Friendship> friendshipOpt = friendshipService.findBetween(currentUserId, otherUserId);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            if (friendshipOpt.isPresent()) {
                Friendship f = friendshipOpt.get();
                res.put("status", f.getStatus().name());
                res.put("friendshipId", f.getId());
                res.put("role", f.getSender().getId().equals(currentUserId) ? "SENDER" : "RECEIVER");
            } else {
                res.put("status", "NONE");
            }
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private Long requireUserId(HttpSession session) {
        UserDTO userDTO = (UserDTO) session.getAttribute("currentUser");
        if (userDTO == null) {
            throw new RuntimeException("Cần đăng nhập để thực hiện");
        }
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return user.getId();
    }

    private Map<String, Object> success(Friendship friendship, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", message);
        res.put("friendshipId", friendship.getId());
        res.put("status", friendship.getStatus().name());
        res.put("senderId", friendship.getSender().getId());
        res.put("receiverId", friendship.getReceiver().getId());
        return res;
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", msg);
        return res;
    }
}
