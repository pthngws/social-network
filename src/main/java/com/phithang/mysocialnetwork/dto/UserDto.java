package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private String token;
    public UserDto(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.firstname = userEntity.getFirstname();
        this.lastname = userEntity.getLastname();
        this.email = userEntity.getEmail();
        this.role = userEntity.getRole();
    }
}
