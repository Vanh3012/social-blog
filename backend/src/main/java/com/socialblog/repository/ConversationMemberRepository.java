package com.socialblog.repository;

import com.socialblog.model.entity.ConversationMember;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    List<ConversationMember> findByUser(User user);
}
