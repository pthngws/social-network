package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.PasswordDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserEntity saveUser(UserEntity user) {
        user.setRole("CLIENT");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public boolean updatePassword(String email, PasswordDto passwordDto)
    {
        UserEntity userEntity = userRepository.findByEmail(email);
        if(userEntity!=null)
        {
            if(passwordEncoder.matches(passwordDto.getOldPassword(),userEntity.getPassword()))
            {
                userEntity.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
                userRepository.save(userEntity);
                return true;
            }
        }
        return false;
    }
}
