package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.FriendshipDto;
import com.phithang.mysocialnetwork.dto.request.FriendshipRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IFriendshipService {
    FriendshipEntity save(FriendshipRequest request);


    boolean acceptFriendRequest(FriendshipRequest friendshipDto);

    FriendshipEntity findBySenderAndReceiver(UserEntity sender, UserEntity receiver);


    boolean cancelRequest(FriendshipRequest friendshipDto);

    FriendshipEntity findByUser1AndUser2(UserEntity sender, UserEntity receiver);

    List<FriendshipDto> findALlRequest();

    List<FriendshipDto> findAllFriends();
}
