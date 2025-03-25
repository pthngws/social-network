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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UpdateProfileRequest>>> searchUsers(@RequestParam("name") String name) {
        ApiResponse<List<UpdateProfileRequest>> apiResponse = new ApiResponse<>(200, userService.findByFirstnameOrLastnameContaining(name), "Search users successful!");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdateProfileRequest>> getUserById(@PathVariable("id") Long id) {
        UpdateProfileRequest userProfile = userService.getUserProfile(id);

        if (userProfile != null) {
            ApiResponse<UpdateProfileRequest> apiResponse = new ApiResponse<>(200, userProfile, "Get user successful!");
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

}
