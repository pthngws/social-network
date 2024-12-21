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
        } else {
            responseDto.setStatus(400);
            responseDto.setMessage("User not found!");
            responseDto.setData(null);
        }
        return responseDto;
    }

    @PostMapping("/profile/update")
    public ResponseDto<UpdateProfileDto> updateProfile(@RequestBody UpdateProfileDto updateProfileDto) {
        ResponseDto<UpdateProfileDto> responseDto = new ResponseDto<>();

        // Lấy thông tin xác thực từ SecurityContextHolder
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findUserByEmail(authentication.getName());

        if (userEntity != null) {
            // Cập nhật thông tin người dùng
            userEntity.setLastname(updateProfileDto.getLastName());
            userEntity.setFirstname(updateProfileDto.getFirstName()); // Đã sửa lỗi logic
            userService.saveUser(userEntity);

            // Thiết lập phản hồi
            responseDto.setStatus(200);
            responseDto.setMessage("Update profile successful!");
            responseDto.setData(updateProfileDto);
        } else {
            // Phản hồi khi không tìm thấy user
            responseDto.setStatus(404); // Thay đổi mã lỗi từ 400 thành 404
            responseDto.setMessage("User not found!");
            responseDto.setData(null);
        }

        return responseDto;
    }
}
