package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.FriendshipDto;
import com.phithang.mysocialnetwork.dto.request.FriendshipRequestDto;
import com.phithang.mysocialnetwork.dto.response.ResponseDto;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IFriendshipService friendshipService;

    @GetMapping("/requests")
    public ResponseEntity<ResponseDto<List<FriendshipDto>>> getFriendshipRequests() {
        List<FriendshipEntity> friendshipEntities = friendshipService.findALlRequest();
        List<FriendshipDto> friendshipDtos = new ArrayList<>();
        for (FriendshipEntity friendshipEntity : friendshipEntities) {
            friendshipDtos.add(new FriendshipDto(friendshipEntity));
        }
        return ResponseEntity.ok(new ResponseDto<>(200, friendshipDtos, "Success"));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto<List<FriendshipDto>>> getAllFriendships() {
        List<FriendshipEntity> friendshipEntities = friendshipService.findAllFriends();
        List<FriendshipDto> friendshipDtos = new ArrayList<>();
        for (FriendshipEntity friendshipEntity : friendshipEntities) {
            if (friendshipEntity.getUser1().getEmail().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                friendshipEntity.setUser1(friendshipEntity.getUser2());
            }
            friendshipDtos.add(new FriendshipDto(friendshipEntity));
        }
        return ResponseEntity.ok(new ResponseDto<>(200, friendshipDtos, "Success"));
    }

    @PostMapping("/add")
    public ResponseEntity<ResponseDto<Void>> addFriendship(@RequestBody FriendshipRequestDto receiverId) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity sender = userService.findUserByEmail(authentication.getName());
            UserEntity receiver = userService.findById(receiverId.getReceiverId());

            if (sender == null || receiver == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Sender or Receiver not found!"));
            }

            if (sender.equals(receiver)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "You cannot send a friend request to yourself!"));
            }

            FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(sender, receiver);
            if (friendshipEntity != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Friendship already exists!"));
            }

            friendshipService.save(sender, receiver);
            return ResponseEntity.ok(new ResponseDto<>(200, null, "Friend request sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<ResponseDto<Void>> acceptFriendship(@RequestBody FriendshipRequestDto friendshipDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String receiverEmail = authentication.getName();
            UserEntity receiver = userService.findUserByEmail(receiverEmail);

            if (receiver == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Receiver not found!"));
            }

            UserEntity sender = userService.findById(friendshipDto.getReceiverId());
            if (sender == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Sender not found!"));
            }

            FriendshipEntity friendshipEntity = friendshipService.findByUser1AndUser2(sender, receiver);
            if (friendshipEntity == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Friend request not found!"));
            }

            friendshipService.accept(sender, receiver);
            return ResponseEntity.ok(new ResponseDto<>(200, null, "Friend request accepted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<ResponseDto<Void>> cancelFriendship(@RequestBody FriendshipRequestDto friendshipDto) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String receiverEmail = authentication.getName();
            UserEntity receiver = userService.findUserByEmail(receiverEmail);

            if (receiver == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Receiver not found!"));
            }

            UserEntity sender = userService.findById(friendshipDto.getReceiverId());
            if (sender == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Sender not found!"));
            }

            FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(sender, receiver);
            if (friendshipEntity == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto<>(400, null, "Friend request not found!"));
            }

            friendshipService.cancelRequest(friendshipEntity);
            return ResponseEntity.ok(new ResponseDto<>(200, null, "Friend request cancelled successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
