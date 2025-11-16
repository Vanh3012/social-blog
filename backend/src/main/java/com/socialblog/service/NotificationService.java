package com.socialblog.service;

import com.socialblog.model.entity.Comment;
import com.socialblog.model.entity.Notification;
import com.socialblog.model.entity.Post;
import com.socialblog.model.entity.User;
import com.socialblog.model.enums.NotificationType;
import com.socialblog.model.enums.ReactionType;
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

}