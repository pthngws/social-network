package com.phithang.mysocialnetwork.service;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.request.IntrospectRequest;
import com.phithang.mysocialnetwork.dto.request.LoginRequest;
import com.phithang.mysocialnetwork.dto.request.SignupRequest;
import com.phithang.mysocialnetwork.dto.UserDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.text.ParseException;

public interface IAuthenticateService {

    String introspectToken(IntrospectRequest token) throws JOSEException, ParseException;

    UserDto oauth2Login(OidcUser oidcUser, OAuth2User oAuth2User) throws JOSEException;

    String generateToken(UserEntity userEntity) throws JOSEException;

    UserDto login(LoginRequest loginRequest) throws JOSEException;

    boolean saveUser(SignupRequest signupDto);
}
