package com.phithang.mysocialnetwork.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequestDto {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String priority;
    private int progress;
}
