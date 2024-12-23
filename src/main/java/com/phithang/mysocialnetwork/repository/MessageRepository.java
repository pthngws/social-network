package com.phithang.mysocialnetwork.repository;


import com.phithang.mysocialnetwork.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}
