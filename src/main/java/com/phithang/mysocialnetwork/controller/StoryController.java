package com.phithang.mysocialnetwork.controller;

import com.phithang.mysocialnetwork.dto.request.StoryRequestDto;
import com.phithang.mysocialnetwork.dto.response.ApiResponse;
import com.phithang.mysocialnetwork.dto.response.StoryInteractionResponseDto;
import com.phithang.mysocialnetwork.dto.response.StoryResponseDto;
import com.phithang.mysocialnetwork.service.Impl.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<StoryResponseDto>>> getAllStories()
    {
        List<StoryResponseDto> list = storyService.findAll();
        ApiResponse<List<StoryResponseDto>> apiResponse = new ApiResponse<>(200, list, "Story created successfully!");
        return ResponseEntity.status(201).body(apiResponse);
    }

    // Đăng story mới
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<StoryResponseDto>> createStory(@ModelAttribute StoryRequestDto request) {
        StoryResponseDto story = storyService.createStory(request);
        ApiResponse<StoryResponseDto> apiResponse = new ApiResponse<>(201, story, "Story created successfully!");
        return ResponseEntity.status(201).body(apiResponse);
    }

    // Xóa story
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, null, "Story deleted successfully!");
        return ResponseEntity.ok(apiResponse);
    }

    // Xem story
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponseDto>> viewStory(@PathVariable Long id) {
        StoryResponseDto story = storyService.viewStory(id);
        ApiResponse<StoryResponseDto> apiResponse = new ApiResponse<>(200, story, "Story fetched successfully!");
        return ResponseEntity.ok(apiResponse);
    }

    // Xem danh sách người xem và người thả tương tác
    @GetMapping("/{id}/interactions")
    public ResponseEntity<ApiResponse<List<StoryInteractionResponseDto>>> getStoryInteractions(@PathVariable Long id) {
        List<StoryInteractionResponseDto> interactions = storyService.getStoryInteractions(id);
        ApiResponse<List<StoryInteractionResponseDto>> apiResponse = new ApiResponse<>(200, interactions, "Interactions fetched successfully!");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{id}/reactions")
    public ResponseEntity<ApiResponse<Boolean>> reactStoryInteractions(
            @PathVariable Long id,
            @RequestParam String reactionType) {

        Boolean isReact = storyService.reactStory(id, reactionType);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(200, isReact, "Story reacted successfully!");
        return ResponseEntity.ok(apiResponse);
    }

}
