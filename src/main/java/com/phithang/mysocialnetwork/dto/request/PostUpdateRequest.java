package com.phithang.mysocialnetwork.dto.request;

import com.phithang.mysocialnetwork.dto.MediaDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostUpdateRequest {
    private Long id;
    private String content;
    private List<MediaDto> media; // Danh sách media mới
    private List<String> mediaToDelete; // Danh sách URL media cần xóa
}