package com.phithang.mysocialnetwork.service;

import com.phithang.mysocialnetwork.dto.response.MusicResponseDto;

import java.util.List;

public interface IMusicService {
    List<MusicResponseDto> getAllMusic();

    MusicResponseDto findById(Long id);
}
