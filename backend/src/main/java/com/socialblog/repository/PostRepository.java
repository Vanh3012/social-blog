package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);

    List<Post> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);

    @Query("SELECT p FROM Post p WHERE p.visibility = 'PUBLIC' OR " +
            "(p.visibility = 'FRIENDS' AND p.author.id IN :friendIds) OR " +
            "p.author.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findVisiblePosts(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds);
}