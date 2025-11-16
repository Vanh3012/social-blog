package com.socialblog.repository;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findBySenderOrReceiver(User sender, User receiver);

    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);

    boolean existsBySenderAndReceiverAndStatus(
            User sender,
            User receiver,
            FriendshipStatus status);

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.sender.id = :userId OR f.receiver.id = :userId)
            AND f.status = com.socialblog.model.enums.FriendshipStatus.ACCEPTED
            """)
    List<Friendship> findAcceptedFriends(@Param("userId") Long userId);
}
