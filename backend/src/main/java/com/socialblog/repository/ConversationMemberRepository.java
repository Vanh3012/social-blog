package com.socialblog.repository;

import com.socialblog.model.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {

    // Lấy tất cả conversation mà user tham gia
    List<ConversationMember> findByUser_Id(Long userId);

    // Lấy tất cả user trong một conversation
    List<ConversationMember> findByConversation_Id(Long conversationId);

    // Lấy conversation giữa 2 user (nếu có)
    @Query("""
                SELECT cm1.conversation FROM ConversationMember cm1
                JOIN ConversationMember cm2 ON cm1.conversation.id = cm2.conversation.id
                WHERE cm1.user.id = :userA AND cm2.user.id = :userB
            """)
    List<com.socialblog.model.entity.Conversation> findConversationBetweenUsers(Long userA, Long userB);
}