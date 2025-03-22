package com.phithang.mysocialnetwork.dto.request;

import com.phithang.mysocialnetwork.entity.UserEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneId;

@Data
public class UpdateProfileRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String about;
    private String avatar;
    private LocalDate birthday;
    private String gender;
    private String friendStatus; // Thêm thuộc tính isFriend
    public UpdateProfileRequest() {
    }
    public UpdateProfileRequest(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.firstName = userEntity.getFirstname();
        this.lastName = userEntity.getLastname();
        this.about = userEntity.getAbout();
        this.avatar = userEntity.getImageUrl();
        this.birthday = userEntity.getBirthday().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;

        this.gender = userEntity.getGender();
    }


}
