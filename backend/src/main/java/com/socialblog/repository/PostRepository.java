package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.PostImage;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Lấy tất cả bài viết theo thời gian mới nhất
    List<Post> findAllByOrderByCreatedAtDesc();

    // bài viết public cho khách
    List<Post> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);

    // bài viết của 1 user
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);

    // bài viết của 1 user với visibility cụ thể
    List<Post> findByAuthorAndVisibilityOrderByCreatedAtDesc(User author, Visibility visibility);

    // bài viết hiển thị với user (public + private của chính họ + bạn bè)
    @Query("""
            SELECT p FROM Post p
            WHERE
                p.visibility = com.socialblog.model.enums.Visibility.PUBLIC
                OR (p.visibility = com.socialblog.model.enums.Visibility.PRIVATE AND p.author.id = :userId)
                OR (p.visibility = com.socialblog.model.enums.Visibility.FRIENDS
                    AND (p.author.id = :userId OR p.author.id IN :friendIds))
            ORDER BY p.createdAt DESC
            """)
    List<Post> findVisibleForUser(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds);

}
