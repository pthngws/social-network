package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}
