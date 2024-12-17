package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.ResponseDto;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IUserService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping(value = "/all")
    public ResponseDto<List<UserDto>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info(authentication.getName());
        log.info(authentication.getAuthorities().toString());
        List<UserEntity> userEntities = userService.findAllUsers();
        List<UserDto> userDtos = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            userDtos.add(new UserDto(userEntity));
        }
        ResponseDto<List<UserDto>> responseDto = new ResponseDto<>();
        responseDto.setStatus(200);
        responseDto.setMessage("Get all users successful!");
        responseDto.setData(userDtos);
        return responseDto;
    }

    @GetMapping("/profile")
    public ResponseDto<UserDto> getProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());
        ResponseDto<UserDto> responseDto = new ResponseDto<>();
        if (userEntity != null) {
            responseDto.setStatus(200);
            responseDto.setMessage("Get profile successful!");
            responseDto.setData(new UserDto(userEntity));
        }
        else {
            responseDto.setStatus(400);
            responseDto.setMessage("User not found!");
            responseDto.setData(null);
        }
        return responseDto;
    }
}
