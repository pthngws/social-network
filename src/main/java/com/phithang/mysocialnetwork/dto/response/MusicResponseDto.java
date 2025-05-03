package com.phithang.mysocialnetwork.dto.response;

import lombok.Data;



@Data
public class MusicResponseDto {
    private Long id;

    private String title;

    private String url; // URL của file nhạc

    private String artist;

    private String thumbnail; // URL của ảnh thumbnail

}
