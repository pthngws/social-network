package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.entity.MessageEntity;

public interface IMessageService {
    boolean saveMessage(MessageEntity messageEntity);
}
