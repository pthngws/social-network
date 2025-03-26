package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.FriendshipDto;
import com.phithang.mysocialnetwork.dto.request.FriendshipRequest;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.FriendshipRepository;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendshipService implements IFriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public FriendshipEntity save(FriendshipRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity sender = userService.findUserByEmail(authentication.getName());
        UserEntity receiver = userService.findById(request.getReceiverId());
        if (sender == null || receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        if (sender.equals(receiver)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        FriendshipEntity existingFriendship = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (existingFriendship != null) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_ALREADY_SENT);
        }

        FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setUser1(sender);
        friendshipEntity.setUser2(receiver);
        friendshipEntity.setStatus("PENDING");
        friendshipEntity.setRequestTimestamp(java.time.LocalDateTime.now());

        try {
            notificationService.createAndSendNotification(
                    receiver,
                    sender.getFirstname() + " " + sender.getLastname() + " đã gửi lời mời kết bạn.", null
            );
            return friendshipRepository.save(friendshipEntity);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_FAILED);
        }
    }

    @Override
    public boolean acceptFriendRequest(FriendshipRequest friendshipDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String receiverEmail = authentication.getName();
        UserEntity receiver = userService.findUserByEmail(receiverEmail);
        UserEntity sender = userService.findById(friendshipDto.getReceiverId());
        if (sender == null) {
            return false;
        }

        FriendshipEntity friendshipEntity = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (friendshipEntity == null) {
            return false;
        }

        friendshipEntity.setStatus("ACCEPTED");
        friendshipEntity.setRequestTimestamp(java.time.LocalDateTime.now());

        try {
            notificationService.createAndSendNotification(
                    sender,
                    receiver.getFirstname() + " " + receiver.getLastname() + " đã chấp nhận lời mời kết bạn.", null
            );
            friendshipRepository.save(friendshipEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_FAILED);
        }
    }

    @Override
    public FriendshipEntity findBySenderAndReceiver(UserEntity sender, UserEntity receiver) {
        FriendshipEntity friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        return friendship;
    }

    @Override
    public boolean cancelRequest(FriendshipRequest friendshipDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String receiverEmail = authentication.getName();
        UserEntity receiver = userService.findUserByEmail(receiverEmail);
        UserEntity sender = userService.findById(friendshipDto.getReceiverId());
        if (sender == null) {
            return false;
        }

        FriendshipEntity friendshipEntity = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (friendshipEntity == null) {
            return false;
        }

        try {
            friendshipRepository.delete(friendshipEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_FAILED);
        }
    }

    @Override
    public FriendshipEntity findByUser1AndUser2(UserEntity sender, UserEntity receiver) {
        FriendshipEntity friendship = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (friendship == null) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }
        return friendship;
    }

    @Override
    public List<FriendshipDto> findALlRequest() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        List<FriendshipEntity> friendshipEntities = friendshipRepository.findAllRequests(userEntity);
        List<FriendshipDto> friendshipDtos = new ArrayList<>();
        for (FriendshipEntity friendshipEntity : friendshipEntities) {
            friendshipDtos.add(new FriendshipDto(friendshipEntity));
        }
        return friendshipDtos;
    }

    @Override
    public List<FriendshipDto> findAllFriends() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        List<FriendshipEntity> friendshipEntities = friendshipRepository.findAllFriends(userEntity);
        List<FriendshipDto> friendshipDtos = new ArrayList<>();
        for (FriendshipEntity friendshipEntity : friendshipEntities) {
            if (friendshipEntity.getUser1().getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                friendshipEntity.setUser1(friendshipEntity.getUser2());
            }
            friendshipDtos.add(new FriendshipDto(friendshipEntity));
        }
        return friendshipDtos;
    }
}