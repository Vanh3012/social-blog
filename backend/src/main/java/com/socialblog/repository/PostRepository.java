package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Lấy tất cả bài viết theo thời gian mới nhất
    List<Post> findAllByOrderByCreatedAtDesc();

    // bài viết public cho khách
    List<Post> findByVisibilityOrderByCreatedAtDesc(Visibility visibility);

    // bài viết của 1 user
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
}
