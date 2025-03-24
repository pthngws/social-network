package com.phithang.mysocialnetwork.dto;

import lombok.Data;

@Data
public class MediaDto {
    private byte[] url; // Dùng byte[] để lưu dữ liệu file thô
    private String type;
}
