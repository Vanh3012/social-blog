package com.socialblog.repository;

import com.socialblog.model.entity.Message;
import com.socialblog.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
}
