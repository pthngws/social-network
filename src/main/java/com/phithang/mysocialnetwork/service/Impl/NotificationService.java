package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.entity.PostEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.NotificationRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public List<NotifyResponse> getNotification() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        return notificationRepository.findAllByUser(user)
                .stream()
                .map(this::mapToNotifyResponse)
                .collect(Collectors.toList());
    }

    private NotifyResponse mapToNotifyResponse(NotificationEntity notification) {
        NotifyResponse response = new NotifyResponse();
        response.setContent(notification.getContent());
        response.setDate(notification.getTimestamp());
        response.setIsRead(notification.getIsread());
        return response;
    }

    public void sendRealTimeNotification(NotificationEntity notification) {
        if (notification == null) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        NotifyResponse response = mapToNotifyResponse(notification);
        String destination = "/topic/notifications/" + notification.getUser().getEmail();
        messagingTemplate.convertAndSend(destination, response);
    }

    public void createAndSendNotification(UserEntity user, String content, PostEntity post) {
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setContent(content);
        notification.setIsread(0);
        notification.setTimestamp(java.time.LocalDateTime.now());
        notification.setPost(post);

        try {
            notificationRepository.save(notification);
            sendRealTimeNotification(notification);
        } catch (Exception e) {
            throw new AppException(ErrorCode.NOTIFICATION_CREATION_FAILED);
        }
    }
}
