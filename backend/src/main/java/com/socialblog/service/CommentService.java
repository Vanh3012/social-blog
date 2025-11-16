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

    public void createComment(CommentRequest request, User user) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Comment c = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(user)
                .build();

        commentRepository.save(c);
    }

    public void deleteComment(Long id, User user) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

        if (!c.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không được xóa bình luận này");
        }

        commentRepository.delete(c);
    }

    public void updateComment(Long id, String content, User user) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

        if (!c.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không được sửa bình luận này");
        }

        c.setContent(content);
        commentRepository.save(c);
    }

    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}
