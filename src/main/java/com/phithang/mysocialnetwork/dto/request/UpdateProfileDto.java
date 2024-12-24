package com.phithang.mysocialnetwork.dto.request;

import com.phithang.mysocialnetwork.entity.UserEntity;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String about;
    private String avatar;
    private Date birthday;
    private String gender;

    public UpdateProfileDto() {
    }
    public UpdateProfileDto(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.firstName = userEntity.getFirstname();
        this.lastName = userEntity.getLastname();
        this.about = userEntity.getAbout();
        this.avatar = userEntity.getImageUrl();
        this.birthday = userEntity.getBirthday();
        this.gender = userEntity.getGender();
    }


}
