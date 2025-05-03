package com.phithang.mysocialnetwork.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StoryRequestDto {
    private MultipartFile media;
    private Long musicId;
}