package com.phithang.mysocialnetwork.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class NotifyResponse {
    private String content;
    private int isRead;
    private LocalDateTime date;
}
