package com.socialblog.repository;

import com.socialblog.model.entity.Notification;
import com.socialblog.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);

    int countByReceiverAndIsReadFalse(User receiver);
}
