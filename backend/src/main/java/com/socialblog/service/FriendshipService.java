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

    // ==================== SEND REQUEST ====================
    public Friendship sendRequest(Long senderId, Long receiverId) {

        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Không thể kết bạn với chính mình");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        // Kiểm tra quan hệ đã tồn tại chưa
        Optional<Friendship> exist = friendshipRepository.findBetween(senderId, receiverId);

        if (exist.isPresent()) {
            Friendship f = exist.get();

            switch (f.getStatus()) {
                case PENDING -> throw new RuntimeException("Đã gửi lời mời trước đó");
                case ACCEPTED -> throw new RuntimeException("Hai bạn đã là bạn bè");
                case BLOCKED -> throw new RuntimeException("Không thể kết bạn vì bị chặn");
                case DECLINED -> {
                    // Cho phép gửi lại: reset trạng thái
                    f.setSender(sender);
                    f.setReceiver(receiver);
                    f.setStatus(FriendshipStatus.PENDING);
                    return friendshipRepository.save(f);
                }
            }
        }

        // Tạo mới
        Friendship friendship = Friendship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        return friendshipRepository.save(friendship);
    }

    // ==================== ACCEPT ====================
    public Friendship accept(Long id, Long currentUserId) {
        Friendship f = findById(id);

        if (!f.getReceiver().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền chấp nhận");
        }

        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Không hợp lệ");
        }

        f.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(f);
    }

    // ==================== DECLINE ====================
    public Friendship decline(Long id, Long currentUserId) {
        Friendship f = findById(id);

        if (!f.getReceiver().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền từ chối");
        }

        f.setStatus(FriendshipStatus.DECLINED);
        return friendshipRepository.save(f);
    }

    // ==================== CANCEL ====================
    public void cancel(Long id, Long currentUserId) {
        Friendship f = findById(id);

        if (!f.getSender().getId().equals(currentUserId)) {
            throw new RuntimeException("Không có quyền hủy");
        }

        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Không hợp lệ");
        }

        friendshipRepository.delete(f);
    }

    // ==================== STATUS ====================
    public Optional<Friendship> findBetween(Long u1, Long u2) {
        return friendshipRepository.findBetween(u1, u2);
    }

    private Friendship findById(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quan hệ"));
    }

    public List<Friendship> listPendingReceived(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        return friendshipRepository.findByReceiverAndStatus(receiver, FriendshipStatus.PENDING);
    }

    
    public List<User> listFriends(Long userId) {
        List<Friendship> accepted = friendshipRepository.findAcceptedFriends(userId);
        return accepted.stream()
                .map(f -> f.getSender().getId().equals(userId) ? f.getReceiver() : f.getSender())
                .distinct()
                .toList();
    }

    public void unfriend(Long userId, Long otherUserId) {
        Friendship friendship = friendshipRepository.findBetween(userId, otherUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quan hệ bạn bè"));
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new RuntimeException("Chỉ hủy khi đang là bạn bè");
        }
        friendshipRepository.delete(friendship);
    }

public List<User> suggestFriends(Long userId, int limit) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        List<Friendship> relations = friendshipRepository.findBySenderOrReceiver(me, me);

        // Danh sách user đã có quan hệ
        var excluded = relations.stream()
                .flatMap(f -> java.util.stream.Stream.of(
                        f.getSender().getId(),
                        f.getReceiver().getId()))
                .collect(java.util.stream.Collectors.toSet());

        excluded.add(userId); // loại chính mình

        List<User> all = userRepository.findAll();

        return all.stream()
                .filter(u -> !excluded.contains(u.getId()))
                .limit(limit)
                .toList();
    }

}
