package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserAndPost(User user, Post post);

    List<Reaction> findByPost(Post post);

    int countByPost(Post post);

    boolean existsByUserAndPost(User user, Post post);
}