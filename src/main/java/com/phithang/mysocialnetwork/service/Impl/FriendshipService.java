package com.phithang.mysocialnetwork.service.Impl;


import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.FriendshipRepository;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FriendshipService implements IFriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Override
    public FriendshipEntity save(UserEntity sender, UserEntity receiver)
    {
        FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setUser1(sender);
        friendshipEntity.setUser2(receiver);
        friendshipEntity.setStatus("PENDING");
        friendshipEntity.setRequestTimestamp(LocalDateTime.now());
        return friendshipRepository.save(friendshipEntity);
    }

    @Override
    public boolean accept(UserEntity sender, UserEntity receiver)
    {
        FriendshipEntity friendshipEntity = friendshipRepository.findByUser1AndUser2(sender, receiver);
        friendshipEntity.setStatus("ACCEPTED");
        friendshipEntity.setRequestTimestamp(LocalDateTime.now());
        return friendshipRepository.save(friendshipEntity) != null;
    }

    @Override
    public FriendshipEntity findBySenderAndReceiver(UserEntity sender, UserEntity receiver)
    {
        return friendshipRepository.findBySenderAndReceiver(sender, receiver);
    }

    @Override
    public Boolean cancelRequest(FriendshipEntity friendshipEntity)
    {
        friendshipRepository.delete(friendshipEntity);
        return true;
    }

    @Override
    public FriendshipEntity findByUser1AndUser2(UserEntity sender, UserEntity receiver)
    {
        return friendshipRepository.findByUser1AndUser2(sender, receiver);
    }
}
