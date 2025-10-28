package com.socialblog.service;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import com.socialblog.repository.FriendshipRepository;
import com.socialblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Friendship sendFriendRequest(Long receiverId, User sender) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (sender.getId().equals(receiverId)) {
            throw new RuntimeException("Không thể kết bạn với chính mình");
        }

        // Kiểm tra đã gửi lời mời chưa
        Optional<Friendship> existing = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        if (existing.isPresent()) {
            throw new RuntimeException("Đã gửi lời mời kết bạn trước đó");
        }

        // Kiểm tra người kia đã gửi lời mời chưa
        Optional<Friendship> reversed = friendshipRepository.findBySenderAndReceiver(receiver, sender);
        if (reversed.isPresent()) {
            throw new RuntimeException("Người này đã gửi lời mời kết bạn cho bạn");
        }

        Friendship friendship = Friendship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Tạo thông báo
        notificationService.createFriendRequestNotification(savedFriendship);

        log.info("User {} sent friend request to user {}", sender.getUsername(), receiver.getUsername());
        return savedFriendship;
    }

    @Transactional
    public Friendship acceptFriendRequest(Long friendshipId, User receiver) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Lời mời này đã được xử lý");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship savedFriendship = friendshipRepository.save(friendship);

        // Tạo thông báo cho người gửi
        notificationService.createFriendAcceptedNotification(savedFriendship);

        log.info("User {} accepted friend request from user {}",
                receiver.getUsername(), friendship.getSender().getUsername());
        return savedFriendship;
    }

    @Transactional
    public void declineFriendRequest(Long friendshipId, User receiver) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn"));

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            throw new RuntimeException("Bạn không có quyền từ chối lời mời này");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendshipRepository.save(friendship);

        log.info("User {} declined friend request from user {}",
                receiver.getUsername(), friendship.getSender().getUsername());
    }

    @Transactional
    public void unfriend(Long friendId, User user) {
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Optional<Friendship> friendship = friendshipRepository.findBySenderAndReceiver(user, friend);
        if (friendship.isEmpty()) {
            friendship = friendshipRepository.findBySenderAndReceiver(friend, user);
        }

        if (friendship.isPresent() && friendship.get().getStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(friendship.get());
            log.info("User {} unfriended user {}", user.getUsername(), friend.getUsername());
        } else {
            throw new RuntimeException("Không tìm thấy mối quan hệ bạn bè");
        }
    }

    @Transactional(readOnly = true)
    public List<User> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriends(user.getId());

        return friendships.stream()
                .map(f -> f.getSender().getId().equals(user.getId())
                        ? f.getReceiver()
                        : f.getSender())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Friendship> getPendingRequests(User user) {
        return friendshipRepository.findByReceiverAndStatus(user, FriendshipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.areFriends(userId1, userId2);
    }

    @Transactional(readOnly = true)
    public FriendshipStatus getFriendshipStatus(User user, User otherUser) {
        Optional<Friendship> friendship = friendshipRepository.findBySenderAndReceiver(user, otherUser);
        if (friendship.isEmpty()) {
            friendship = friendshipRepository.findBySenderAndReceiver(otherUser, user);
        }

        return friendship.map(Friendship::getStatus).orElse(null);
    }
}