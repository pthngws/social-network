package com.phithang.mysocialnetwork.service.Impl;

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
    public FriendshipEntity save(UserEntity sender, UserEntity receiver) {
        if (sender == null || receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        FriendshipEntity existingFriendship = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (existingFriendship != null) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_ALREADY_SENT);
        }
        FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setUser1(sender);
        friendshipEntity.setUser2(receiver);
        friendshipEntity.setStatus("PENDING");
        friendshipEntity.setRequestTimestamp(LocalDateTime.now());
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
    public boolean accept(UserEntity sender, UserEntity receiver) {
        FriendshipEntity friendshipEntity = friendshipRepository.findByUser1AndUser2(sender, receiver);
        if (friendshipEntity == null) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND);
        }
        friendshipEntity.setStatus("ACCEPTED");
        friendshipEntity.setRequestTimestamp(LocalDateTime.now());
        try {
            notificationService.createAndSendNotification(
                    sender,
                    receiver.getFirstname() + " " + receiver.getLastname() + " đã chấp nhận lời mời kết bạn.", null
            );
            return friendshipRepository.save(friendshipEntity) != null;
        } catch (Exception e) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_FAILED);
        }
    }

    @Override
    public FriendshipEntity findBySenderAndReceiver(UserEntity sender, UserEntity receiver) {
        FriendshipEntity friendship = friendshipRepository.findBySenderAndReceiver(sender, receiver);
        if (friendship == null) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }
        return friendship;
    }

    @Override
    public Boolean cancelRequest(FriendshipEntity friendshipEntity) {
        if (friendshipEntity == null) {
            throw new AppException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND);
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
    public List<FriendshipEntity> findALlRequest() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        return friendshipRepository.findAllRequests(userEntity);
    }

    @Override
    public List<FriendshipEntity> findAllFriends() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        return friendshipRepository.findAllFriends(userEntity);
    }
}