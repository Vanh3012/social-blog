package com.socialblog.service;

import com.socialblog.dto.CommentRequest;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.CommentRepository;
import com.socialblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // ====================== TẠO COMMENT / REPLY ======================
    public Comment addComment(CommentRequest request, User author) {

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy comment cha"));
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(post)
                .parentComment(parent)
                .build();

        Comment saved = commentRepository.save(comment);

        // cập nhật số comment cho bài viết
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return saved;
    }

    // ====================== LẤY DANH SÁCH COMMENT ======================
    public List<Comment> getCommentsOfPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}
