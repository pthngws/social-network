package com.phithang.mysocialnetwork.dto;

import com.phithang.mysocialnetwork.entity.MediaDto;
import lombok.Data;

import java.util.List;

@Data
public class PostDto {
    private String content;
    private List<MediaDto> media;

}
