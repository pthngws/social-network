package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    public List<UserEntity> findAllUsers();
    public UserEntity findUserByEmail(String email);

    UserEntity saveUser(UserEntity user);

    boolean updatePassword(PasswordDto passwordDto);


    boolean updateProfile(UpdateProfileRequest updateProfileRequest, MultipartFile avatarFile);

    UserEntity findById(Long id);

    List<UserEntity> findByFirstnameOrLastnameContaining(String name);
}
