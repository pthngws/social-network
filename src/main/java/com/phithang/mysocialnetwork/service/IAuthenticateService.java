package com.phithang.mysocialnetwork.service;

import com.nimbusds.jose.JOSEException;
import com.phithang.mysocialnetwork.dto.IntrospectDto;
import com.phithang.mysocialnetwork.entity.UserEntity;


import java.text.ParseException;

public interface IAuthenticateService {

    String introspectToken(IntrospectDto token) throws JOSEException, ParseException;

    String generateToken(UserEntity userEntity) throws JOSEException;
}
