package com.socialblog.service;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;

    // Thêm hoặc cập nhật reaction (toggle)
    @Transactional
    public long addOrUpdateReaction(ReactionRequest request, User user) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        Reaction existingReaction = reactionRepository.findByPostAndUser(post, user).orElse(null);

        if (existingReaction != null) {
            if (existingReaction.getType() == request.getType()) {
                // Toggle off
                reactionRepository.delete(existingReaction);
            } else {
                existingReaction.setType(request.getType());
                reactionRepository.save(existingReaction);
            }
        } else {
            Reaction newReaction = Reaction.builder()
                    .post(post)
                    .user(user)
                    .type(request.getType())
                    .build();
            reactionRepository.save(newReaction);
        }

        long totalReactions = reactionRepository.countByPost(post);
        post.setLikeCount((int) totalReactions);
        postRepository.save(post);
        return totalReactions;
    }

    // Xóa reaction
    @Transactional
    public long removeReaction(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));

        reactionRepository.findByPostAndUser(post, user)
                .ifPresent(reactionRepository::delete);

        long totalReactions = reactionRepository.countByPost(post);
        post.setLikeCount((int) totalReactions);
        postRepository.save(post);
        return totalReactions;
    }

    public long countReactionsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết!"));
        return reactionRepository.countByPost(post);
    }

    public String getUserReactionForPost(Long postId, User user) {
        return reactionRepository.findReactionType(postId, user.getId()).orElse(null);
    }

    public List<Reaction> getReactionsByPost(Post post) {
        return reactionRepository.findAll().stream().filter(r -> r.getPost().equals(post)).toList();
    }

    // Top reactions
    public List<ReactionCount> topReactions(Post post, int limit) {
        List<Object[]> raw = reactionRepository.countGroupByType(post.getId());
        return raw.stream()
                .map(arr -> new ReactionCount((com.socialblog.model.enums.ReactionType) arr[0], (Long) arr[1]))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .limit(limit)
                .toList();
    }

    public record ReactionCount(com.socialblog.model.enums.ReactionType type, long count) {
    }
}
