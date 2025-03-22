package com.phithang.mysocialnetwork.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken, long ttlInSeconds) {
        redisTemplate.opsForValue().set("REFRESH_TOKEN:" + email, refreshToken, ttlInSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get("REFRESH_TOKEN:" + email);
    }

    public String getEmailByRefreshToken(String refreshToken) {
        // Tìm email tương ứng với refresh token
        for (String key : redisTemplate.keys("REFRESH_TOKEN:*")) {
            String storedToken = redisTemplate.opsForValue().get(key);
            if (refreshToken.equals(storedToken)) {
                return key.replace("REFRESH_TOKEN:", "");
            }
        }
        return null;
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete("REFRESH_TOKEN:" + email);
    }
}