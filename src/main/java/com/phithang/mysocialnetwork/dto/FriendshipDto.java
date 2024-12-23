package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import lombok.Data;

@Data
public class FriendshipDto {
    private Long id;
    private String status;
    private java.time.LocalDateTime requestTimestamp;
    private UserDto user;

    public FriendshipDto(FriendshipEntity entity) {
        this.id = entity.getId();
        this.status = entity.getStatus();
        this.user = new UserDto(entity.getUser1());
        this.requestTimestamp = entity.getRequestTimestamp();
    }

}
