package com.phithang.mysocialnetwork.dto.request;

import com.phithang.mysocialnetwork.dto.MediaDto;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateDto {
    private Long id;
    private String content;

}
