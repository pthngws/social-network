package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.ResponseDto;
import com.phithang.mysocialnetwork.dto.UpdateProfileDto;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IUserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;

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

    @GetMapping("/profile")
    public ResponseDto<UserDto> getProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());
        if (userEntity != null) {
            return new ResponseDto<>(200,new UserDto(userEntity),"Get profile successful!");
        }
        return new ResponseDto<>(400,null,"Get profile failed!");
    }

    @PostMapping("/profile/update")
    public ResponseDto<UpdateProfileDto> updateProfile(@RequestBody UpdateProfileDto updateProfileDto) {
        if(userService.updateProfile(updateProfileDto)) {
            return new ResponseDto<>(200,updateProfileDto,"Update profile successful!");
        }
        return new ResponseDto<>(400,null,"Update profile failed!");
    }
}
