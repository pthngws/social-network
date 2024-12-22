package com.phithang.mysocialnetwork.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostUpdateDto {
    private Long id;
    private String content;
    private List<MediaDto> media;

}
