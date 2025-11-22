package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    // Tìm reaction của một user trên một bài post cụ thể
    Optional<Reaction> findByPostAndUser(Post post, User user);

    // Lấy tất cả reaction của một bài post
    List<Reaction> findByPost(Post post);

    // Đếm số reaction của một bài post
    long countByPost(Post post);

    // Đếm số reaction của một loại trên một bài post
    long countByPostAndType(Post post, ReactionType type);

    // Tìm loại reaction của một user trên một bài post cụ thể
    @Query("SELECT r.type FROM Reaction r WHERE r.post.id = :postId AND r.user.id = :userId")
    Optional<String> findReactionType(Long postId, Long userId);

    // Thống kê số lượng reaction theo loại trên một bài post
    @Query("SELECT r.type, COUNT(r) FROM Reaction r WHERE r.post.id = :postId GROUP BY r.type")
    List<Object[]> countGroupByType(Long postId);

}
