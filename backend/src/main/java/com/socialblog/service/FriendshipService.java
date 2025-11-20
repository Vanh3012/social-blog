package com.socialblog.service;

import com.socialblog.model.entity.Friendship;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.FriendshipStatus;
import com.socialblog.repository.FriendshipRepository;
import com.socialblog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public Friendship sendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Không thể kết bạn với chính mình");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        Optional<Friendship> existing = friendshipRepository.findBetween(senderId, receiverId);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.PENDING) {
                throw new RuntimeException("Đã gửi lời mời trước đó");
            }
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Hai bạn đã là bạn bè");
            }
            if (f.getStatus() == FriendshipStatus.BLOCKED) {
                throw new RuntimeException("Kết bạn bị chặn");
            }
            // Nếu đã bị từ chối thì cho phép gửi lại, cập nhật sender/receiver mới
            f.setSender(sender);
            f.setReceiver(receiver);
            f.setStatus(FriendshipStatus.PENDING);
            return friendshipRepository.save(f);
        }

        Friendship friendship = Friendship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();
        return friendshipRepository.save(friendship);
    }

    public Friendship accept(Long friendshipId, Long currentUserId) {
        Friendship friendship = findById(friendshipId);
        if (!friendship.getReceiver().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền chấp nhận lời mời này");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Lời mời đã được xử lý");
        }
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public Friendship decline(Long friendshipId, Long currentUserId) {
        Friendship friendship = findById(friendshipId);
        if (!friendship.getReceiver().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền từ chối lời mời này");
        }
        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendshipRepository.save(friendship);
    }

    public void cancel(Long friendshipId, Long currentUserId) {
        Friendship friendship = findById(friendshipId);
        if (!friendship.getSender().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền hủy lời mời này");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Lời mời đã được xử lý");
        }
        friendshipRepository.delete(friendship);
    }

    public Optional<Friendship> findBetween(Long user1, Long user2) {
        return friendshipRepository.findBetween(user1, user2);
    }

    public List<Friendship> listAccepted(Long userId) {
        return friendshipRepository.findAcceptedFriends(userId);
    }

    public List<Friendship> listPendingReceived(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return friendshipRepository.findByReceiverAndStatus(receiver, FriendshipStatus.PENDING);
    }

    public List<User> suggestFriends(Long userId, int limit) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        List<Friendship> relations = friendshipRepository.findBySenderOrReceiver(me, me);
        // Loại trừ tất cả người đã có quan hệ (dù pending/accepted/blocked) và chính mình.
        var excluded = relations.stream()
                .flatMap(f -> java.util.stream.Stream.of(f.getSender().getId(), f.getReceiver().getId()))
                .collect(java.util.stream.Collectors.toSet());
        excluded.add(userId);

        List<User> all = userRepository.findAll();
        return all.stream()
                .filter(u -> !excluded.contains(u.getId()))
                .limit(Math.max(limit, 0))
                .toList();
    }

    private Friendship findById(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời"));
    }
}
