package com.socialblog.service;

import com.socialblog.dto.ReactionRequest;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.ReactionType;
import com.socialblog.repository.PostRepository;
import com.socialblog.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {

        private final ReactionRepository reactionRepository;
        private final PostRepository postRepository;

        @Transactional
        public long toggleReaction(ReactionRequest request, User user) {

                Post post = postRepository.findById(request.getPostId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

                ReactionType newType = request.getType();

                var optional = reactionRepository.findByPostAndUser(post, user);

                // Nếu đã từng react
                if (optional.isPresent()) {
                        Reaction old = optional.get();

                        if (old.getType() == newType) {
                                // Nếu bấm lại reaction cũ -> hủy reaction
                                reactionRepository.delete(old);
                        } else {
                                // Đổi loại reaction
                                old.setType(newType);
                                reactionRepository.save(old);
                        }

                } else {
                        // Chưa react → tạo mới
                        Reaction r = Reaction.builder()
                                        .post(post)
                                        .user(user)
                                        .type(newType)
                                        .build();
                        reactionRepository.save(r);
                }

                // Cập nhật tổng số react
                long total = reactionRepository.findByPost(post).size();
                post.setLikeCount((int) total);
                postRepository.save(post);

                return total;
        }

        @Transactional
        public long removeReaction(Long postId, User user) {

                Post post = postRepository.findById(postId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

                var optional = reactionRepository.findByPostAndUser(post, user);

                if (optional.isPresent()) {
                        reactionRepository.delete(optional.get());
                }

                // Cập nhật tổng số react
                long total = reactionRepository.findByPost(post).size();
                post.setLikeCount((int) total);
                postRepository.save(post);

                return total;
        }
}