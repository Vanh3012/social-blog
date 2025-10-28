package com.socialblog.service;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public Reaction addOrUpdateReaction(ReactionRequest request, User user) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Optional<Reaction> existingReaction = reactionRepository.findByUserAndPost(user, post);

        Reaction reaction;
        boolean isNew = false;

        if (existingReaction.isPresent()) {
            // Cập nhật reaction hiện có
            reaction = existingReaction.get();
            reaction.setType(request.getType());
            log.info("User {} updated reaction on post {}", user.getUsername(), post.getId());
        } else {
            // Tạo reaction mới
            reaction = Reaction.builder()
                    .type(request.getType())
                    .user(user)
                    .post(post)
                    .build();
            isNew = true;

            // Tăng like count
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);

            log.info("User {} added reaction to post {}", user.getUsername(), post.getId());
        }

        Reaction savedReaction = reactionRepository.save(reaction);

        // Tạo thông báo nếu là reaction mới và không phải tác giả
        if (isNew && !post.getAuthor().getId().equals(user.getId())) {
            notificationService.createReactionNotification(savedReaction);
        }

        return savedReaction;
    }

    @Transactional
    public void removeReaction(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Optional<Reaction> reaction = reactionRepository.findByUserAndPost(user, post);

        reaction.ifPresent(r -> {
            reactionRepository.delete(r);

            // Giảm like count
            if (post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
                postRepository.save(post);
            }

            log.info("User {} removed reaction from post {}", user.getUsername(), postId);
        });
    }

    @Transactional(readOnly = true)
    public int getReactionCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        return reactionRepository.countByPost(post);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReacted(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
        return reactionRepository.existsByUserAndPost(user, post);
    }
}