package com.socialblog.repository;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

        // Tìm quan hệ giữa 2 user, kể cả đảo chiều
        @Query("""
                         SELECT f FROM Friendship f
                         WHERE
                             (f.sender.id = :u1 AND f.receiver.id = :u2)
                             OR
                             (f.sender.id = :u2 AND f.receiver.id = :u1)
                        """)
        Optional<Friendship> findBetween(Long u1, Long u2);

        // Lấy bạn bè (ACCEPTED)
        @Query("""
                         SELECT f FROM Friendship f
                         WHERE
                            (f.sender.id = :userId OR f.receiver.id = :userId)
                            AND f.status = com.socialblog.model.enums.FriendshipStatus.ACCEPTED
                        """)
        List<Friendship> findAcceptedFriends(Long userId);

        // Lời mời user nhận được (pending)
        List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

        // Tất cả quan hệ liên quan đến user
        List<Friendship> findBySenderOrReceiver(User sender, User receiver);
}
