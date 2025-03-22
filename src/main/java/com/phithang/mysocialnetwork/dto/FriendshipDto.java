package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import lombok.Data;

@Data
public class FriendshipDto {
    private Long id;
    private String status;
    private java.time.LocalDateTime requestTimestamp;
    private UpdateProfileRequest user;

    public FriendshipDto(FriendshipEntity entity) {
        this.id = entity.getId();
        this.status = entity.getStatus();
        this.user = new UpdateProfileRequest(entity.getUser1());
        this.requestTimestamp = entity.getRequestTimestamp();
    }

}
