package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.response.MusicResponseDto;
import com.phithang.mysocialnetwork.entity.Music;
import com.phithang.mysocialnetwork.repository.MusicRepository;
import com.phithang.mysocialnetwork.service.IMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MusicService implements IMusicService {

    @Autowired
    private MusicRepository musicRepository;

    @Override
    public List<MusicResponseDto> getAllMusic() {
        List<Music> musicList = musicRepository.findAll();
        List<MusicResponseDto> musicResponseDtoList = new ArrayList<>();
        for (Music music : musicList) {
            MusicResponseDto musicResponseDto = new MusicResponseDto();
            musicResponseDto.setId(music.getId());
            musicResponseDto.setTitle(music.getTitle());
            musicResponseDto.setArtist(music.getArtist());
            musicResponseDto.setUrl(music.getUrl());
            musicResponseDto.setThumbnail(music.getThumbnail());
            musicResponseDtoList.add(musicResponseDto);
        }
        return musicResponseDtoList;
    }

    @Override
    public MusicResponseDto findById(Long id)
    {
        Music music = musicRepository.findById(id).get();
        MusicResponseDto musicResponseDto = new MusicResponseDto();
        musicResponseDto.setId(music.getId());
        musicResponseDto.setTitle(music.getTitle());
        musicResponseDto.setArtist(music.getArtist());
        musicResponseDto.setUrl(music.getUrl());
        musicResponseDto.setThumbnail(music.getThumbnail());
        return musicResponseDto;
    }
}
