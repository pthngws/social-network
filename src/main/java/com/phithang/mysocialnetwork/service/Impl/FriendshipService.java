package com.phithang.mysocialnetwork.service.Impl;


import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.FriendshipRepository;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendshipService implements IFriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

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

    @Override
    public List<FriendshipEntity> findALlRequest()
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        return friendshipRepository.findAllByUser2(userEntity);
    }
}
