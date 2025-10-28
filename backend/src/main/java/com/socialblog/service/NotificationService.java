package com.socialblog.service;

import com.socialblog.model.entity.*;
import com.socialblog.model.enums.NotificationType;
import com.socialblog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createCommentNotification(Comment comment) {
        String message = comment.getAuthor().getFullName() + " đã bình luận bài viết của bạn";

        Notification notification = Notification.builder()
                .message(message)
                .type(NotificationType.COMMENT)
                .isRead(false)
                .user(comment.getAuthor())
                .receiver(comment.getPost().getAuthor())
                .post(comment.getPost())
                .comment(comment)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created comment notification for user {}", comment.getPost().getAuthor().getUsername());
        return saved;
    }

    @Transactional
    public Notification createReactionNotification(Reaction reaction) {
        String reactionText = getReactionText(reaction.getType());
        String message = reaction.getUser().getFullName() + " đã " + reactionText + " bài viết của bạn";

        Notification notification = Notification.builder()
                .message(message)
                .type(NotificationType.LIKE)
                .isRead(false)
                .user(reaction.getUser())
                .receiver(reaction.getPost().getAuthor())
                .post(reaction.getPost())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created reaction notification for user {}", reaction.getPost().getAuthor().getUsername());
        return saved;
    }

    @Transactional
    public Notification createFriendRequestNotification(Friendship friendship) {
        String message = friendship.getSender().getFullName() + " đã gửi lời mời kết bạn";

        Notification notification = Notification.builder()
                .message(message)
                .type(NotificationType.FRIEND_REQUEST)
                .isRead(false)
                .user(friendship.getSender())
                .receiver(friendship.getReceiver())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created friend request notification for user {}", friendship.getReceiver().getUsername());
        return saved;
    }

    @Transactional
    public Notification createFriendAcceptedNotification(Friendship friendship) {
        String message = friendship.getReceiver().getFullName() + " đã chấp nhận lời mời kết bạn của bạn";

        Notification notification = Notification.builder()
                .message(message)
                .type(NotificationType.FRIEND_REQUEST)
                .isRead(false)
                .user(friendship.getReceiver())
                .receiver(friendship.getSender())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created friend accepted notification for user {}", friendship.getSender().getUsername());
        return saved;
    }

    @Transactional
    public void createMessageNotification(Message message) {
        // Gửi thông báo cho tất cả thành viên trong conversation trừ sender
        message.getConversation().getMembers().stream()
                .filter(member -> !member.getUser().getId().equals(message.getSender().getId()))
                .forEach(member -> {
                    String notifMessage = message.getSender().getFullName() + " đã gửi tin nhắn cho bạn";

                    Notification notification = Notification.builder()
                            .message(notifMessage)
                            .type(NotificationType.MESSAGE)
                            .isRead(false)
                            .user(message.getSender())
                            .receiver(member.getUser())
                            .build();

                    notificationRepository.save(notification);
                    log.info("Created message notification for user {}", member.getUser().getUsername());
                });
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByReceiverAndIsReadOrderByCreatedAtDesc(user, false);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(User user) {
        return notificationRepository.countByReceiverAndIsRead(user, false);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh dấu thông báo này");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("User {} marked notification {} as read", user.getUsername(), notificationId);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("User {} marked all notifications as read", user.getUsername());
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa thông báo này");
        }

        notificationRepository.delete(notification);
        log.info("User {} deleted notification {}", user.getUsername(), notificationId);
    }

    private String getReactionText(com.socialblog.model.enums.ReactionType type) {
        switch (type) {
            case LIKE:
                return "thích";
            case LOVE:
                return "yêu thích";
            case HAHA:
                return "bày tỏ cảm xúc haha về";
            case WOW:
                return "bày tỏ cảm xúc wow về";
            case SAD:
                return "bày tỏ cảm xúc buồn về";
            case ANGRY:
                return "bày tỏ cảm xúc phẫn nộ về";
            default:
                return "bày tỏ cảm xúc về";
        }
    }
}