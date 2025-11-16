package com.socialblog.controller;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.model.entity.User;
import com.socialblog.service.ReactionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reaction")
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addReaction(
            @RequestBody ReactionRequest request, 
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Bạn phải đăng nhập để thực hiện hành động này!"));
        }

        long totalReactions = reactionService.toggleReaction(request, user);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalReactions", totalReactions
        ));
    }

    @DeleteMapping("/remove/{postId}")
    public ResponseEntity<Map<String, Object>> removeReaction(
            @PathVariable Long postId,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Bạn phải đăng nhập!"));
        }

        long totalReactions = reactionService.removeReaction(postId, user);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalReactions", totalReactions
        ));
    }
}