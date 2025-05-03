package com.phithang.mysocialnetwork.controller;


import com.phithang.mysocialnetwork.dto.request.UpdateProfileRequest;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.dto.response.MusicResponseDto;
import com.phithang.mysocialnetwork.service.IMusicService;
import com.phithang.mysocialnetwork.service.Impl.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/musics")
@RestController
public class MusicController {

    @Autowired
    private IMusicService musicService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<MusicResponseDto>>> getAllMusics() {
        ApiResponse<List<MusicResponseDto>> apiResponse = new ApiResponse<>(200, musicService.getAllMusic(), "successful!");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MusicResponseDto>> getMusicById(@PathVariable("id") Long id) {
        ApiResponse<MusicResponseDto> apiResponse = new ApiResponse<>(200, musicService.findById(id), "successful!");
        return ResponseEntity.ok(apiResponse);
    }
}
