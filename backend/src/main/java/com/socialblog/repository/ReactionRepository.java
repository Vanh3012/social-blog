package com.socialblog.repository;

import com.socialblog.model.entity.Reaction;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByPost(Post post);

    Optional<Reaction> findByPostAndUser(Post post, User user);
}
