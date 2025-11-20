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

    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

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

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.sender.id = :user1 AND f.receiver.id = :user2)
            OR (f.sender.id = :user2 AND f.receiver.id = :user1)
            """)
    Optional<Friendship> findBetween(@Param("user1") Long user1, @Param("user2") Long user2);
}
