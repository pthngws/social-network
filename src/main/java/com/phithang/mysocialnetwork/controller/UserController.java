package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.request.UpdateProfileDto;
import com.phithang.mysocialnetwork.dto.response.ResponseDto;
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
    public ResponseEntity<ResponseDto<List<UpdateProfileDto>>> searchUsers(@RequestParam("name") String name) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = userService.findUserByEmail(authentication.getName());

        List<UserEntity> users = userService.findByFirstnameOrLastnameContaining(name);
        List<UpdateProfileDto> userDtos = new ArrayList<>();

        for (UserEntity userEntity : users) {
            if (!userEntity.getId().equals(currentUser.getId())) {
                String friendStatus = getFriendStatus(userEntity, currentUser);

                if (userEntity.getBirthday() == null)
                    userEntity.setBirthday(new Date());

                UpdateProfileDto updateProfileDto = new UpdateProfileDto(userEntity);
                updateProfileDto.setFriendStatus(friendStatus);
                userDtos.add(updateProfileDto);
            }
        }

        ResponseDto<List<UpdateProfileDto>> responseDto = new ResponseDto<>(200, userDtos, "Search users successful!");
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<UpdateProfileDto>> getUserById(@PathVariable("id") Long id) {
        UserEntity userEntity = userService.findById(id);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = userService.findUserByEmail(authentication.getName());

        if (userEntity != null) {
            String friendStatus = getFriendStatus(userEntity, currentUser);

            UpdateProfileDto updateProfileDto = new UpdateProfileDto(userEntity);
            updateProfileDto.setFriendStatus(friendStatus);

            ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(200, updateProfileDto, "Get user successful!");
            return ResponseEntity.ok(responseDto);
        }
        ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(404, null, "User not found!");
        return ResponseEntity.status(404).body(responseDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseDto<UpdateProfileDto>> getProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());

        if (userEntity != null) {
            ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(200, new UpdateProfileDto(userEntity), "Get profile successful!");
            return ResponseEntity.ok(responseDto);
        }
        ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(404, null, "Profile not found!");
        return ResponseEntity.status(404).body(responseDto);
    }

    @PutMapping("/profile/update")
    public ResponseEntity<ResponseDto<UpdateProfileDto>> updateProfile(
            @ModelAttribute UpdateProfileDto updateProfileDto,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) {

        if (userService.updateProfile(updateProfileDto, avatarFile)) {
            ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(200, updateProfileDto, "Update profile successful!");
            return ResponseEntity.ok(responseDto);
        }
        ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>(400, null, "Update profile failed!");
        return ResponseEntity.badRequest().body(responseDto);
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
