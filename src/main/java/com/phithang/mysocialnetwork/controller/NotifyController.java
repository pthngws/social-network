package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.service.Impl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotifyResponse>>> getNotifications() {
        List<NotificationEntity> notifications = notificationService.getNotification();

        if (notifications.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, null, "No notifications found"));
        }

        List<NotifyResponse> responses = notifications.stream().map(notification -> {
            NotifyResponse response = new NotifyResponse();
            response.setContent(notification.getContent());
            response.setDate(notification.getTimestamp());
            response.setIsRead(notification.getIsread());
            response.setPostId(notification.getPost().getId());
            return response;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(200, responses, "Success"));
    }
}
