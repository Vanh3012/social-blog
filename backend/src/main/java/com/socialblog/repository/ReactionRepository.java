package com.socialblog.repository;

import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByPostAndUser(Post post, User user);

    List<Reaction> findByPost(Post post);

    long countByPost(Post post);

    long countByPostAndType(Post post, ReactionType type);
}
