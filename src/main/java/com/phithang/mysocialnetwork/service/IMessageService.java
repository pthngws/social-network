package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import java.util.List;
import java.util.Map;

public interface IMessageService {

    List<Map<String, Object>> findDistinctParticipantsByUserId(Long receiverId);

    MessageEntity save(MessageDto chatEntity);

    List<Map<String, Object>> findMessagesBetweenUsers(Long senderId, Long receiverId);
}
