package com.phithang.mysocialnetwork.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phithang.mysocialnetwork.dto.request.PasswordDto;
import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.FriendshipRepository;
import com.phithang.mysocialnetwork.repository.UserRepository;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_NOT_EXIST, "Lưu người dùng thất bại");
        }
    }

    @Override
    public boolean updatePassword(PasswordDto passwordDto) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        if (!passwordEncoder.matches(passwordDto.getOldPassword(), userEntity.getPassword())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mật khẩu cũ không đúng");
        }
        userEntity.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        try {
            userRepository.save(userEntity);
            return true;
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_NOT_EXIST, "Cập nhật mật khẩu thất bại");
        }
    }

    @Override
    public boolean updateProfile(UpdateProfileRequest updateProfileRequest, MultipartFile avatarFile) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                if (avatarFile.getSize() > 10 * 1024 * 1024) { // Giới hạn 10MB
                    throw new AppException(ErrorCode.MEDIA_SIZE_EXCEEDED);
                }
                Map uploadResult = cloudinary.uploader().upload(avatarFile.getBytes(),
                        ObjectUtils.asMap("resource_type", "image"));
                String imageUrl = uploadResult.get("url").toString();
                userEntity.setImageUrl(imageUrl);
            } else if (updateProfileRequest.getAvatar() != null) {
                userEntity.setImageUrl(updateProfileRequest.getAvatar());
            }

            userEntity.setLastname(updateProfileRequest.getLastName());
            userEntity.setFirstname(updateProfileRequest.getFirstName());
            userEntity.setAbout(updateProfileRequest.getAbout());
            userEntity.setBirthday(java.sql.Date.valueOf(updateProfileRequest.getBirthday()));
            userEntity.setGender(updateProfileRequest.getGender());

            userRepository.save(userEntity);
            return true;
        } catch (IOException e) {
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        } catch (Exception e) {
            throw new AppException(ErrorCode.PROFILE_NOT_FOUND, "Cập nhật profile thất bại");
        }
    }

    @Override
    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
    }
    @Override
    public UpdateProfileRequest getUserProfile(Long id) {
        UserEntity userEntity = findById(id);
        if (userEntity == null) {
            return null;
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = findUserByEmail(authentication.getName());

        String friendStatus = getFriendStatus(userEntity, currentUser);

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(userEntity);
        updateProfileRequest.setFriendStatus(friendStatus);

        return updateProfileRequest;
    }
    @Override
    public List<UpdateProfileRequest> findByFirstnameOrLastnameContaining(String name) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity currentUser = userRepository.findByEmail(authentication.getName());

        List<UserEntity> users = userRepository.findByFirstnameOrLastnameContaining(name);
        List<UpdateProfileRequest> userDtos = new ArrayList<>();

        for (UserEntity userEntity : users) {
            if (!userEntity.getId().equals(currentUser.getId())) {
                String friendStatus = getFriendStatus(userEntity, currentUser);

                if (userEntity.getBirthday() == null)
                    userEntity.setBirthday(new Date());

                UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(userEntity);
                updateProfileRequest.setFriendStatus(friendStatus);
                userDtos.add(updateProfileRequest);
            }
        }
        return userDtos;
    }

    // Helper method to get friend status
    private String getFriendStatus(UserEntity userEntity, UserEntity currentUser) {
        FriendshipEntity friendshipEntity = friendshipRepository.findBySenderAndReceiver(userEntity, currentUser);
        String friendStatus = "NULL";
        if (friendshipEntity != null) {
            friendStatus = friendshipEntity.getStatus();
            if (friendStatus.equals("PENDING")) {
                if (friendshipEntity.getUser1().equals(userEntity)) {
                    friendStatus = "SENT_BY_OTHER";
                }
            }
        }
        return friendStatus;
    }
}