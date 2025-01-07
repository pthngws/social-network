package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.NotificationEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findAllByUser(UserEntity user);
}
