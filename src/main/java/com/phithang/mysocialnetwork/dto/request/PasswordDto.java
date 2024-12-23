package com.phithang.mysocialnetwork.dto.request;

import lombok.Data;

@Data
public class PasswordDto {
    private String oldPassword;
    private String newPassword;
}
