package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.NotificationRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public List<NotificationEntity> getNotification() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(email);
        return notificationRepository.findAllByUser(user);
    }

    public void sendRealTimeNotification(NotificationEntity notification) {
        NotifyResponse response = new NotifyResponse();
        response.setContent(notification.getContent());
        response.setDate(notification.getTimestamp());
        response.setIsRead(notification.getIsread());
        response.setPostId(notification.getPost() != null ? notification.getPost().getId() : null);

        // Sử dụng email thay vì ID
        String destination = "/topic/notifications/" + notification.getUser().getEmail();
        messagingTemplate.convertAndSend(destination, response);
    }

    public void createAndSendNotification(UserEntity user, String content, PostEntity post) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setContent(content);
        notification.setIsread(0);
        notification.setTimestamp(java.time.LocalDateTime.now());
        notification.setPost(post);

        notificationRepository.save(notification);
        sendRealTimeNotification(notification);
    }
}