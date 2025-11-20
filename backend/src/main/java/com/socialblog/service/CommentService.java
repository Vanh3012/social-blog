package com.socialblog.service;

import com.socialblog.dto.CommentRequest;
import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.repository.CommentRepository;
import com.socialblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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

    public void deleteComment(Long commentId, Long currentUserId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bình luận"));

        Post post = comment.getPost();

        // chỉ người viết comment hoặc chủ bài viết được xóa
        if (!comment.getAuthor().getId().equals(currentUserId)
                && !post.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này");
        }

        // Nếu là comment cha → xoá replies luôn
        if (!comment.getReplies().isEmpty()) {
            comment.getReplies().forEach(reply -> commentRepository.delete(reply));
        }

        commentRepository.delete(comment);
    }
}
