package com.socialblog.repository;

import com.socialblog.model.entity.Notification;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
}
