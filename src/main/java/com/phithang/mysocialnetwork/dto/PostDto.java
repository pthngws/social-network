package com.phithang.mysocialnetwork.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostDto {
    private String content;
    private List<MediaDto> media;

}
