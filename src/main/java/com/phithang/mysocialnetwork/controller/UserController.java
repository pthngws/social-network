package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import com.phithang.mysocialnetwork.service.IUserService;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendshipService friendshipService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UpdateProfileRequest>>> searchUsers(@RequestParam("name") String name) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = userService.findUserByEmail(authentication.getName());

        List<UserEntity> users = userService.findByFirstnameOrLastnameContaining(name);
        List<UpdateProfileRequest> userDtos = new ArrayList<>();

        for (UserEntity userEntity : users) {
            if (!userEntity.getId().equals(currentUser.getId())) {
                String friendStatus = getFriendStatus(userEntity, currentUser);

                if (userEntity.getBirthday() == null)
                    userEntity.setBirthday(new Date());

                UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(userEntity);
                updateProfileRequest.setFriendStatus(friendStatus);
                userDtos.add(updateProfileRequest);
            }
        }

        ApiResponse<List<UpdateProfileRequest>> apiResponse = new ApiResponse<>(200, userDtos, "Search users successful!");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdateProfileRequest>> getUserById(@PathVariable("id") Long id) {
        UserEntity userEntity = userService.findById(id);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = userService.findUserByEmail(authentication.getName());

        if (userEntity != null) {
            String friendStatus = getFriendStatus(userEntity, currentUser);

            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(userEntity);
            updateProfileRequest.setFriendStatus(friendStatus);

            ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(200, updateProfileRequest, "Get user successful!");
            return ResponseEntity.ok(apiResponse);
        }
        ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(404, null, "User not found!");
        return ResponseEntity.status(404).body(apiResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UpdateProfileRequest>> getProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());

        if (userEntity != null) {
            ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(200, new UpdateProfileRequest(userEntity), "Get profile successful!");
            return ResponseEntity.ok(apiResponse);
        }
        ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(404, null, "Profile not found!");
        return ResponseEntity.status(404).body(apiResponse);
    }

    @PutMapping("/profile/update")
    public ResponseEntity<ApiResponse<UpdateProfileRequest>> updateProfile(
            @ModelAttribute UpdateProfileRequest updateProfileRequest,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) {

        if (userService.updateProfile(updateProfileRequest, avatarFile)) {
            ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(200, updateProfileRequest, "Update profile successful!");
            return ResponseEntity.ok(apiResponse);
        }
        ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(400, null, "Update profile failed!");
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Helper method to get friend status
    private String getFriendStatus(UserEntity userEntity, UserEntity currentUser) {
        FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(userEntity, currentUser);
        String friendStatus = "NULL";
        if (friendshipEntity != null) {
            friendStatus = friendshipEntity.getStatus();
            if (friendStatus.equals("PENDING")) {
                if (friendshipEntity.getUser1().equals(userEntity)) {
                    friendStatus = "SENT_BY_OTHER";
                }
            }
        }
        return friendStatus;
    }
}
