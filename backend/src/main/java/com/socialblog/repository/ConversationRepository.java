package com.socialblog.repository;

import com.socialblog.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Tìm conversation riêng giữa 2 người dùng
    @Query("""
                SELECT c FROM Conversation c
                JOIN c.members m1
                JOIN c.members m2
                WHERE c.group = false
                AND m1.user.id = :userA
                AND m2.user.id = :userB
            """)
    List<Conversation> findPrivateConversationBetween(Long userA, Long userB);

    // Lấy tất cả conversation user tham gia (sắp xếp theo updatedAt)
    @Query("""
                SELECT c FROM Conversation c
                JOIN c.members m
                WHERE m.user.id = :userId
                ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findAllByUserIdOrderByUpdated(Long userId);
}