package com.phithang.mysocialnetwork.service;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.IntrospectDto;
import com.phithang.mysocialnetwork.dto.LoginDto;
import com.phithang.mysocialnetwork.dto.SignupDto;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;


import java.text.ParseException;

public interface IAuthenticateService {

    String introspectToken(IntrospectDto token) throws JOSEException, ParseException;

    String generateToken(UserEntity userEntity) throws JOSEException;

    UserDto login(LoginDto loginDto) throws JOSEException;

    boolean saveUser(SignupDto signupDto);
}
