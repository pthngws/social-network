package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.response.ResponseDto;
import com.phithang.mysocialnetwork.dto.request.UpdateProfileDto;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IFriendshipService;
import com.phithang.mysocialnetwork.service.IUserService;
import lombok.extern.slf4j.Slf4j;

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

//    @GetMapping(value = "/all")
//    public ResponseDto<List<UserDto>> getUsers() {
//        var authentication = SecurityContextHolder.getContext().getAuthentication();
//        List<UserEntity> userEntities = userService.findAllUsers();
//        List<UserDto> userDtos = new ArrayList<>();
//        for (UserEntity userEntity : userEntities) {
//            userDtos.add(new UserDto(userEntity));
//        }
//        ResponseDto<List<UserDto>> responseDto = new ResponseDto<>();
//        responseDto.setStatus(200);
//        responseDto.setMessage("Get all users successful!");
//        responseDto.setData(userDtos);
//        return responseDto;
//    }

    @GetMapping("/search")
    public ResponseDto<List<UpdateProfileDto>> searchUsers(@RequestParam("name") String name) {
        List<UserEntity> users = userService.findByFirstnameOrLastnameContaining(name);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity2 = userService.findUserByEmail(authentication.getName());
        List<UpdateProfileDto> userDtos = new ArrayList<>();
        for (UserEntity userEntity : users) {
            if(!userEntity.getId().equals(userEntity2.getId())) {
                FriendshipEntity friendshipEntity = friendshipService.findBySenderAndReceiver(userEntity, userEntity2);
                String friendStatus = "NULL";
                if (friendshipEntity != null) {
                    friendStatus = friendshipEntity.getStatus();
                    if (friendStatus.equals("PENDING")) {
                        if (friendshipEntity.getUser1().equals(userEntity)) {
                            friendStatus = "SENT_BY_OTHER";
                        }
                    }
                }
                if (userEntity.getBirthday() == null)
                    userEntity.setBirthday(new Date());
                UpdateProfileDto updateProfileDto = new UpdateProfileDto(userEntity);
                updateProfileDto.setFriendStatus(friendStatus);
                userDtos.add(updateProfileDto);
            }
        }
        return new ResponseDto<>(200,userDtos,"Search users successful!");
    }

    @GetMapping("/{id}")
    public ResponseDto<UpdateProfileDto> getUserById(@PathVariable("id") Long id) {
        UserEntity userEntity = userService.findById(id);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity2 = userService.findUserByEmail(authentication.getName());
        FriendshipEntity friendshipEntity =friendshipService.findBySenderAndReceiver(userEntity, userEntity2);
        String friendStatus = "NULL";
        if(friendshipEntity != null)
        {
            friendStatus = friendshipEntity.getStatus();
            if(friendStatus.equals("PENDING"))
            {
                if(friendshipEntity.getUser1().equals(userEntity))
                {
                    friendStatus = "SENT_BY_OTHER";
                }
            }
        }
        if (userEntity != null) {
            UpdateProfileDto updateProfileDto = new  UpdateProfileDto(userEntity);
            updateProfileDto.setFriendStatus(friendStatus);
            return new ResponseDto<>(200,updateProfileDto,"Get user successful!");
        }
        return new ResponseDto<>(400,null,"Get user failed!");
    }

    @GetMapping("/profile")
    public ResponseDto<UpdateProfileDto> getProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());
        if (userEntity != null) {
            return new ResponseDto<>(200,new UpdateProfileDto(userEntity),"Get profile successful!");
        }
        return new ResponseDto<>(400,null,"Get profile failed!");
    }

    @PutMapping("/profile/update")
    public ResponseDto<UpdateProfileDto> updateProfile(
            @ModelAttribute UpdateProfileDto updateProfileDto,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) {
        if (userService.updateProfile(updateProfileDto, avatarFile)) {
            return new ResponseDto<>(200, updateProfileDto, "Update profile successful!");
        }
        return new ResponseDto<>(400, null, "Update profile failed!");
    }

}
