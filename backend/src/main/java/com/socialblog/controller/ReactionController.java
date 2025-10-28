package com.socialblog.controller;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.ReactionType;
import com.socialblog.repository.UserRepository;
import com.socialblog.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reaction")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {

    private final ReactionService reactionService;
    private final UserRepository userRepository;

    // Thêm hoặc cập nhật reaction
    @PostMapping("/add")
    public String addReaction(
            @RequestParam Long postId,
            @RequestParam ReactionType type,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            ReactionRequest request = new ReactionRequest();
            request.setPostId(postId);
            request.setType(type);

            reactionService.addOrUpdateReaction(request, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã thả cảm xúc!");
            return "redirect:/post/" + postId;

        } catch (Exception e) {
            log.error("Lỗi khi thêm reaction: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/" + postId;
        }
    }

    // Xóa reaction
    @PostMapping("/remove")
    public String removeReaction(
            @RequestParam Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            reactionService.removeReaction(postId, user);

            redirectAttributes.addFlashAttribute("successMessage", "Đã bỏ cảm xúc!");
            return "redirect:/post/" + postId;

        } catch (Exception e) {
            log.error("Lỗi khi xóa reaction: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra!");
            return "redirect:/post/" + postId;
        }
    }
}
