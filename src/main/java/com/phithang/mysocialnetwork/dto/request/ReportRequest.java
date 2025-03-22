package com.phithang.mysocialnetwork.dto.request;

import lombok.Data;

@Data
public class ReportRequest {
    private Long id;
    private String title;
    private Long postId;
}
