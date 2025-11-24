package com.socialblog.repository;

import com.socialblog.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversation_IdOrderByCreatedAtAsc(Long conversationId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.sender.id <> :userId")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    Long countByConversation_IdAndIsReadFalseAndSender_IdNot(Long conversationId, Long userId);
}
