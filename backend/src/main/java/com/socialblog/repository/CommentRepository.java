package com.socialblog.repository;

import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    int countByPost(Post post);
}
