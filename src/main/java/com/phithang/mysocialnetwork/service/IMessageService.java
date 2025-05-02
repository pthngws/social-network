package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.dto.response.ConversationDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IMessageService {

    List<ConversationDto> findDistinctParticipantsByUserId(Long receiverId);

    MessageEntity save(MessageDto chatEntity);

    List<Map<String, Object>> findMessagesBetweenUsers(Long senderId, Long receiverId);


    @Transactional
    void markMessagesAsRead(Long senderId);
}
