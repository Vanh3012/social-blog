package com.socialblog.repository;

import com.socialblog.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Lấy tất cả bình luận của một bài post sắp xếp theo ngày tạo
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
