package com.phithang.mysocialnetwork.dto.request;

import com.phithang.mysocialnetwork.dto.MediaDto;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private String content;
    private List<MediaDto> media;
}
