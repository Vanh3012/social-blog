package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(User author);
}
