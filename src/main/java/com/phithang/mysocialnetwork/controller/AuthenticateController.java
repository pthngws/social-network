package com.phithang.mysocialnetwork.controller;


import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.*;
import com.phithang.mysocialnetwork.dto.request.IntrospectDto;
import com.phithang.mysocialnetwork.dto.request.LoginRequestDto;
import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.SignupDto;
import com.phithang.mysocialnetwork.dto.response.ResponseDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.service.IAuthenticateService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseDto<UserDto> login(@RequestBody LoginRequestDto loginRequest) throws JOSEException {
        UserDto userDto = authenticateService.login(loginRequest);
        if (userDto!=null) {
            return new ResponseDto<>(200,userDto,"Login successful");
        }
        return new ResponseDto<>(400,null,"Invalid username or password");
    }

    @PostMapping("/introspect")
    public ResponseDto<UserDto> introspect(@RequestBody IntrospectDto token) throws JOSEException, ParseException {
        String email = authenticateService.introspectToken(token);
        if (email != null) {
            return new ResponseDto<>(200,null,"Introspect successful");
        }
        return new ResponseDto<>(400,null,"Invalid token");
    }

    @PostMapping("/signup")
    public ResponseDto<UserDto> signup(@RequestBody SignupDto signupDto) {
        UserEntity userEntity = userService.findUserByEmail(signupDto.getEmail());
        if (userEntity != null) {
            return new ResponseDto<>(400,null,"Email already in use");
        }
        if(authenticateService.saveUser(signupDto)) {
            return new ResponseDto<>(200, null, "Signup successful");
        }
        return new ResponseDto<>(400,null,"Invalid email or password");

    }

    @PostMapping("/updatepassword")
    public ResponseEntity<String> changepassword(@RequestBody PasswordDto passwordDto)
    {
        if(userService.updatePassword(passwordDto))
        {
            return ResponseEntity.ok("Update password successful!");
        }
        return ResponseEntity.badRequest().body("Update password failed!");

    }

}
