package com.socialblog.controller;

import com.socialblog.dto.UserDTO;
import com.socialblog.model.entity.Friendship;
import com.socialblog.service.FriendshipService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    // ===================== SEND =====================
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long receiverId, HttpSession session) {
        Long senderId = requireLogin(session);
        Friendship f = friendshipService.sendRequest(senderId, receiverId);
        return ResponseEntity.ok(success("Đã gửi lời mời", f));
    }

    // ===================== ACCEPT =====================
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id, HttpSession session) {
        Long uid = requireLogin(session);
        Friendship f = friendshipService.accept(id, uid);
        return ResponseEntity.ok(success("Đã chấp nhận", f));
    }

    // ===================== DECLINE =====================
    @PostMapping("/{id}/decline")
    public ResponseEntity<?> decline(@PathVariable Long id, HttpSession session) {
        Long uid = requireLogin(session);
        Friendship f = friendshipService.decline(id, uid);
        return ResponseEntity.ok(success("Đã từ chối", f));
    }

    // ===================== CANCEL =====================
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, HttpSession session) {
        Long uid = requireLogin(session);
        friendshipService.cancel(id, uid);
        return ResponseEntity.ok(success("Đã hủy lời mời", null));
    }

    // ===================== UNFRIEND =====================
    @PostMapping("/unfriend/{otherUserId}")
    public ResponseEntity<?> unfriend(@PathVariable Long otherUserId, HttpSession session) {
        Long uid = requireLogin(session);
        friendshipService.unfriend(uid, otherUserId);
        return ResponseEntity.ok(success("Đã hủy kết bạn", null));
    }

    // ===================== UTIL =====================
    private Long requireLogin(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("currentUser");
        if (user == null)
            throw new RuntimeException("Cần đăng nhập");
        return user.getId();
    }

    private Map<String, Object> success(String msg, Friendship f) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", msg);
        if (f != null) {
            map.put("friendshipId", f.getId());
            map.put("status", f.getStatus());
            map.put("senderId", f.getSender().getId());
            map.put("receiverId", f.getReceiver().getId());
        }
        return map;
    }
}
