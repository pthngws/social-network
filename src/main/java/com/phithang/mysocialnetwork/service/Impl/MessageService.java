package com.phithang.mysocialnetwork.service.Impl;


import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.repository.MessageRepository;
import com.phithang.mysocialnetwork.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public boolean saveMessage(MessageEntity messageEntity)
    {
        messageRepository.save(messageEntity);
        return true;
    }
}
