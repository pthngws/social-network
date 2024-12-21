package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;

public interface IFriendshipService {
    FriendshipEntity save(UserEntity sender, UserEntity receiver);

    boolean accept(UserEntity sender, UserEntity receiver);

    FriendshipEntity findBySenderAndReceiver(UserEntity sender, UserEntity receiver);

    Boolean cancelRequest(FriendshipEntity friendshipEntity);

    FriendshipEntity findByUser1AndUser2(UserEntity sender, UserEntity receiver);
}
