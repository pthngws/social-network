package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.UpdateProfileDto;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private Cloudinary cloudinary;

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
        return userRepository.save(user);
    }

    @Override
    public boolean updatePassword(PasswordDto passwordDto)
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
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

    @Override
    public boolean updateProfile(UpdateProfileDto updateProfileDto, MultipartFile avatarFile) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity != null) {
            try {
                // Upload ảnh lên Cloudinary nếu có file
                if (avatarFile != null && !avatarFile.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(avatarFile.getBytes(),
                            ObjectUtils.asMap("resource_type", "image"));
                    String imageUrl = uploadResult.get("url").toString();
                    userEntity.setImageUrl(imageUrl); // Lưu URL ảnh vào database
                } else if (updateProfileDto.getAvatar() != null) {
                    userEntity.setImageUrl(updateProfileDto.getAvatar()); // Sử dụng URL từ DTO nếu có
                }

                // Cập nhật các thông tin khác
                userEntity.setLastname(updateProfileDto.getLastName());
                userEntity.setFirstname(updateProfileDto.getFirstName());
                userEntity.setAbout(updateProfileDto.getAbout());
                userEntity.setBirthday(java.sql.Date.valueOf(updateProfileDto.getBirthday()));
                userEntity.setGender(updateProfileDto.getGender());

                userRepository.save(userEntity);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    @Override
    public UserEntity findById(Long id)
    {
        return userRepository.findById(id).orElse(null);
    }


    @Override
    public List<UserEntity> findByFirstnameOrLastnameContaining(String name)
    {
        return userRepository.findByFirstnameOrLastnameContaining(name);
    }
}
