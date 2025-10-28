package com.socialblog.service;

import com.socialblog.dto.CommentRequest;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.CommentRepository;
import com.socialblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Transactional
    public Comment createComment(CommentRequest request, User author) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));

        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElse(null);
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(post)
                .parentComment(parentComment)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Tăng comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // Tạo thông báo cho tác giả bài viết
        if (!post.getAuthor().getId().equals(author.getId())) {
            notificationService.createCommentNotification(savedComment);
        }

        log.info("User {} commented on post {}", author.getUsername(), post.getId());
        return savedComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new RuntimeException("Bạn không có quyền xóa comment này");
        }

        Post post = comment.getPost();

        // Giảm comment count
        if (post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
        }

        commentRepository.delete(comment);
        log.info("Comment {} deleted by user {}", commentId, user.getUsername());
    }

    @Transactional
    public Comment updateComment(Long commentId, String content, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy comment"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa comment này");
        }

        comment.setContent(content);
        comment.setEdited(true);

        return commentRepository.save(comment);
    }
}