package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    List<UserEntity> findAllUsers();
    UserEntity findUserByEmail(String email);

    UserEntity saveUser(UserEntity user);

    boolean updatePassword(PasswordDto passwordDto);


    boolean updateProfile(UpdateProfileRequest updateProfileRequest, MultipartFile avatarFile);

    UserEntity findById(Long id);

    UpdateProfileRequest getUserProfile(Long id);

    List<UpdateProfileRequest> findByFirstnameOrLastnameContaining(String name);
}
