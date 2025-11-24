package com.socialblog.service;

import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Notification;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
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

    /**
     * Tạo thông báo khi có comment
     */
    @Transactional
    public Notification createCommentNotification(User postAuthor, User commenter, Comment comment) {
        if (postAuthor == null || commenter == null || comment == null) {
            log.warn("⚠️ Thiếu dữ liệu để tạo thông báo comment");
            return null;
        }

        if (postAuthor.getId().equals(commenter.getId())) {
            log.info("⏭️ Bỏ qua - Comment của chính tác giả");
            return null;
        }

        try {
            Notification notification = new Notification();
            notification.setReceiver(postAuthor);
            notification.setUser(commenter);
            String displayName = commenter.getFullName() != null ? commenter.getFullName() : commenter.getUsername();
            notification.setMessage(displayName + " đã bình luận bài viết của bạn");
            notification.setType(NotificationType.COMMENT);
            notification.setPost(comment.getPost());
            notification.setComment(comment);
            notification.setRead(false);

            Notification saved = notificationRepository.save(notification);
            log.info("✅ Tạo thông báo comment cho {}", postAuthor.getUsername());
            return saved;
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo thông báo comment: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tạo thông báo khi có reaction
     */
    @Transactional
    public Notification createReactionNotification(User postAuthor, User reactor, Post post) {
        if (postAuthor == null || reactor == null || post == null) {
            log.warn("⚠️ Thiếu dữ liệu để tạo thông báo reaction");
            return null;
        }

        if (postAuthor.getId().equals(reactor.getId())) {
            log.info("⏭️ Bỏ qua - Reaction của chính tác giả");
            return null;
        }

        try {
            Notification notification = new Notification();
            notification.setReceiver(postAuthor);
            notification.setUser(reactor);
            String displayName = reactor.getFullName() != null ? reactor.getFullName() : reactor.getUsername();
            notification.setMessage(displayName + " đã thích bài viết của bạn");
            notification.setType(NotificationType.LIKE);
            notification.setPost(post);
            notification.setRead(false);

            Notification saved = notificationRepository.save(notification);
            log.info("✅ Tạo thông báo reaction cho {}", postAuthor.getUsername());
            return saved;
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo thông báo reaction: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tất cả thông báo của user
     */
    public List<Notification> getUserNotifications(User user) {
        if (user == null) {
            return List.of();
        }
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy thông báo chưa đọc của user
     */
    public List<Notification> getUnreadNotifications(User user) {
        if (user == null) {
            return List.of();
        }
        return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Đếm thông báo chưa đọc
     */
    public int getUnreadCount(User user) {
        if (user == null) {
            return 0;
        }
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("✅ Đánh dấu thông báo {} đã đọc", notificationId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi đánh dấu đã đọc: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi đánh dấu đã đọc");
        }
    }

    /**
     * Xóa thông báo
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            log.info("✅ Xóa thông báo {}", notificationId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi xóa thông báo: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi xóa thông báo");
        }
    }
}
