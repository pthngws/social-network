package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.repository.MessageRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IMessageService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private MessageRepository chatRepository;

    @Autowired
    private IUserService userService;

    @Override
    public List<Map<String, Object>> findDistinctParticipantsByUserId(Long receiverId) {
        return chatRepository.findDistinctParticipantsByUserId(receiverId);
    }


    @Override
    public MessageEntity save(MessageDto chatEntity) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setContent(chatEntity.getContent());
        messageEntity.setReceiver(userService.findById(chatEntity.getReceiverId()));
        messageEntity.setSender(userService.findById(chatEntity.getSenderId()));
        messageEntity.setTimestamp(chatEntity.getTimestamp());

        return chatRepository.save(messageEntity);
    }

    @Override
    public List<MessageEntity> findAll() {
        return chatRepository.findAll();
    }

}
