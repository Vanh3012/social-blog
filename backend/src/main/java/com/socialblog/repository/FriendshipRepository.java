package com.socialblog.repository;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    List<Friendship> findBySenderAndStatus(User sender, FriendshipStatus status);

    Optional<Friendship> findBySenderAndReceiver(User sender, User receiver);
}
