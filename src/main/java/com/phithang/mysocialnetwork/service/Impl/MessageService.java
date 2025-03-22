package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.MessageRepository;
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

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Map<String, Object>> findDistinctParticipantsByUserId(Long receiverId) {
        UserEntity receiver = userService.findById(receiverId);
        if (receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        return chatRepository.findDistinctParticipantsByUserId(receiverId);
    }

    @Override
    public MessageEntity save(MessageDto chatEntity) {
        UserEntity sender = userService.findById(chatEntity.getSenderId());
        UserEntity receiver = userService.findById(chatEntity.getReceiverId());
        if (sender == null || receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setContent(chatEntity.getContent());
        messageEntity.setReceiver(receiver);
        messageEntity.setSender(sender);
        messageEntity.setTimestamp(chatEntity.getTimestamp());
        try {
            notificationService.createAndSendNotification(
                    receiver,
                    sender.getFirstname() + " " + sender.getLastname() + " đã gửi tin nhắn.", null
            );
            return chatRepository.save(messageEntity);
        } catch (Exception e) {
            throw new AppException(ErrorCode.MESSAGE_CREATION_FAILED);
        }
    }

    @Override
    public List<MessageEntity> findAll() {
        return chatRepository.findAll();
    }
}