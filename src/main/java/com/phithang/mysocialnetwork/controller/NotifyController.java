package com.phithang.mysocialnetwork.controller;


import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Autowired
    private INotificationService notificationService;


    @GetMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(new ApiResponse<>(200, null, "All notifications marked as read"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotifyResponse>>> getNotifications() {
        List<NotifyResponse> notifications = notificationService.getNotification();
        String message = notifications.isEmpty() ? "No notifications found" : "Success";
        return ResponseEntity.ok(new ApiResponse<>(200, notifications, message));
    }
}

