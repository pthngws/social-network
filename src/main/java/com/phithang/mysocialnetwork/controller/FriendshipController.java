package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.FriendshipDto;
import com.phithang.mysocialnetwork.dto.ResponseDto;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IFriendshipService friendshipService;

    @PostMapping("/add")
    public ResponseDto addFriendship(@RequestBody FriendshipDto receiverId) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity sender = userService.findUserByEmail(authentication.getName());
            UserEntity receiver = userService.findById(receiverId.getReceiverId());
            if (sender == null || receiver == null) {
                return new ResponseDto(400, "Sender or Receiver not found!", null);
            }

            if (sender.equals(receiver)) {
                return new ResponseDto(400, "You cannot send a friend request to yourself!", null);
            }

            FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(sender, receiver);
            if (friendshipEntity != null) {
                return new ResponseDto(400, "Friendship already exists!", null);
            }
            friendshipService.save(sender,receiver);

            return new ResponseDto(200, "Friend request sent successfully!", null);
        } catch (Exception e) {
            return new ResponseDto(500, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @PostMapping("/accept")
    public ResponseDto acceptFriendship(@RequestBody FriendshipDto friendshipDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String receiverEmail = authentication.getName();
            UserEntity receiver = userService.findUserByEmail(receiverEmail);

            if (receiver == null) {
                return new ResponseDto(400, "Receiver not found!", null);
            }

            UserEntity sender = userService.findById(friendshipDto.getReceiverId());
            if (sender == null) {
                return new ResponseDto(400, "Sender not found!", null);
            }

            FriendshipEntity friendshipEntity = friendshipService.findByUser1AndUser2(sender, receiver);
            if (friendshipEntity == null) {
                return new ResponseDto(400, "Friend request not found!", null);
            }
            friendshipService.accept(sender,receiver);

            return new ResponseDto(200, "Friend request accepted successfully!", null);
        } catch (Exception e) {
            return new ResponseDto(500, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @PostMapping("/cancel")
    public ResponseDto cancelFriendship(@RequestBody FriendshipDto friendshipDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String receiverEmail = authentication.getName();
            UserEntity receiver = userService.findUserByEmail(receiverEmail);

            if (receiver == null) {
                return new ResponseDto(400, "Receiver not found!", null);
            }

            UserEntity sender = userService.findById(friendshipDto.getReceiverId());
            if (sender == null) {
                return new ResponseDto(400, "Sender not found!", null);
            }

            FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(sender, receiver);
            if (friendshipEntity == null) {
                return new ResponseDto(400, "Friend request not found!", null);
            }

            friendshipService.cancelRequest(friendshipEntity);

            return new ResponseDto(200, "Friend request cancelled successfully!", null);
        } catch (Exception e) {
            return new ResponseDto(500, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }
}
