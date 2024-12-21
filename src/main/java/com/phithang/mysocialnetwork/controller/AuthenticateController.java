package com.phithang.mysocialnetwork.controller;


import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticateController {
    @Autowired
    private IUserService userService;

    @Autowired
    private IAuthenticateService authenticateService;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseDto<UserDto> login(@RequestBody LoginDto loginRequest) throws JOSEException {
        ResponseDto responseDto = new ResponseDto();

        UserEntity userEntity = userService.findUserByEmail(loginRequest.getEmail());
        if (userEntity != null) {
            if (passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
                String token = authenticateService.generateToken(userEntity);
                UserDto userDto = new UserDto(userEntity);
                userDto.setToken(token);
                responseDto.setStatus(200);
                responseDto.setMessage("Login Successful!");
                responseDto.setData(userDto);
            } else {
                responseDto.setStatus(400);
                responseDto.setMessage("Password is incorrect");
                responseDto.setData(null);
            }
        } else {
            responseDto.setStatus(400);
            responseDto.setMessage("Email not found");
            responseDto.setData(null);
        }
        return responseDto;
    }

    @PostMapping("/introspect")
    public ResponseDto<UserDto> introspect(@RequestBody IntrospectDto token) throws JOSEException, ParseException {
        ResponseDto responseDto = new ResponseDto();
        String email = authenticateService.introspectToken(token);
        if (email != null) {
            UserEntity userEntity = userService.findUserByEmail(email);
            UserDto userDto = new UserDto(userEntity);
            responseDto.setStatus(200);
            responseDto.setMessage("Introspect Successful!");
            responseDto.setData(userDto);
        } else {
            responseDto.setStatus(400);
            responseDto.setMessage("Token is invalid");
            responseDto.setData(null);
        }
        return responseDto;
    }



    @PostMapping("/signup")
    public ResponseDto<UserDto> signup(@RequestBody SignupDto signupDto) {
        ResponseDto responseDto = new ResponseDto();
        UserEntity userEntity = userService.findUserByEmail(signupDto.getEmail());
        if (userEntity != null) {
            responseDto.setStatus(400);
            responseDto.setMessage("Email already exists!");
            responseDto.setData(null);
        } else {
            userEntity = signupDto.toUserEntity();
            userEntity.setRole("CLIENT");
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            userService.saveUser(userEntity);
            responseDto.setStatus(200);
            responseDto.setMessage("Signup Successful!");
            responseDto.setData(signupDto);
        }
        return responseDto;
    }

    @PostMapping("/updatepassword")
    public ResponseEntity<String> changepassword(@RequestBody PasswordDto passwordDto)
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        if(userService.updatePassword(email,passwordDto))
        {
            return ResponseEntity.ok("Update password successful!");
        }
        return ResponseEntity.badRequest().body("Update password failed!");

    }

}
