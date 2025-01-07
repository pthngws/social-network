package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.dto.response.ResponseDto;
import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.Impl.NotificationService;
import com.phithang.mysocialnetwork.service.Impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseDto<List<NotifyResponse>> getNotifications() {
        List<NotificationEntity> notifications = notificationService.getNotification();
        if (notifications.isEmpty()) {
            return new ResponseDto<>(200,null,"Failed");
        }
        List<NotifyResponse> responses = new ArrayList<>();
        for (NotificationEntity notification : notifications) {
            NotifyResponse response = new NotifyResponse();
            response.setContent(notification.getContent());
            response.setDate(notification.getTimestamp());
            response.setIsRead(notification.getIsread());
            response.setPostId(notification.getPost().getId());
            responses.add(response);
        }
        return new ResponseDto<>(200,responses,"Success");
    }
}
