package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.UserEntity;
import lombok.Data;


@Data
public class SignupDto {
    private String email;
    private String password;
    private String firstname;
    private String lastname;

    public UserEntity toUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword(password);
        userEntity.setFirstname(firstname);
        userEntity.setLastname(lastname);
        return userEntity;
    }
}
