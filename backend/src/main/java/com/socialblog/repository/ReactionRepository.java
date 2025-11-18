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

    Optional<Reaction> findByPostAndUser(Post post, User user);

    List<Reaction> findByPost(Post post);

    long countByPost(Post post);

    long countByPostAndType(Post post, ReactionType type);

    @Query("SELECT r.type FROM Reaction r WHERE r.post.id = :postId AND r.user.id = :userId")
    Optional<String> findReactionType(Long postId, Long userId);

}
