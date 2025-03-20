package com.phithang.mysocialnetwork.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dx1irzekz", // Thay "your_cloud_name" bằng tên cloud của bạn
                "api_key", "711467652869468",       // Thay "your_api_key" bằng API key của bạn
                "api_secret", "ief3iw56kf8qP9zsjAPFf9WZ3Zc" // Thay "your_api_secret" bằng API secret của bạn
        ));
    }
}
