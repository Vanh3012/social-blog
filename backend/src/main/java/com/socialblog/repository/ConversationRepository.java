package com.socialblog.repository;

import com.socialblog.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.members m WHERE m.user.id = :userId")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c JOIN c.members m1 JOIN c.members m2 " +
            "WHERE m1.user.id = :userId1 AND m2.user.id = :userId2 AND c.group = false")
    Optional<Conversation> findPrivateConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}