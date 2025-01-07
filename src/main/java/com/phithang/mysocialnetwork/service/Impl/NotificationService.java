package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.NotificationRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<NotificationEntity> getNotification()
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email);

        return notificationRepository.findAllByUser(user);


    }
}
