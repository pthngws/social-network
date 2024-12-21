package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.PasswordDto;
import com.phithang.mysocialnetwork.entity.UserEntity;

import java.util.List;

public interface IUserService {
    public List<UserEntity> findAllUsers();
    public UserEntity findUserByEmail(String email);
    public UserEntity saveUser(UserEntity user);

    boolean updatePassword(String email, PasswordDto passwordDto);

    UserEntity findById(Long id);
}
