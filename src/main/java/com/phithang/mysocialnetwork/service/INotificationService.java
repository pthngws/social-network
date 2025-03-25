package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.response.NotifyResponse;
import com.phithang.mysocialnetwork.entity.NotificationEntity;

import java.util.List;

public interface INotificationService {

    List<NotifyResponse> getNotification();
}
