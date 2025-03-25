package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.FriendshipDto;
import com.phithang.mysocialnetwork.dto.request.FriendshipRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
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
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getFriendshipRequests() {
        return ResponseEntity.ok(new ApiResponse<>(200, friendshipService.findALlRequest(), "Success"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getAllFriendships() {
        return ResponseEntity.ok(new ApiResponse<>(200, friendshipService.findAllFriends(), "Success"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addFriendship(@RequestBody FriendshipRequest request) {
        try {
            friendshipService.save(request);
            return ResponseEntity.ok(new ApiResponse<>(200, null, "Friend request sent successfully!"));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Boolean>> acceptFriendship(@RequestBody FriendshipRequest friendshipDto) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200, friendshipService.acceptFriendRequest(friendshipDto), "Friend request accepted!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Boolean>> cancelFriendship(@RequestBody FriendshipRequest friendshipDto) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(200,friendshipService.cancelRequest(friendshipDto), "Friend request cancelled!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, null, "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
