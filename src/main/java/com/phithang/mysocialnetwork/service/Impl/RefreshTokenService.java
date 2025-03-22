package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String email, String refreshToken, long ttlInSeconds) {
        try {
            redisTemplate.opsForValue().set("REFRESH_TOKEN:" + email, refreshToken, ttlInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "Lưu refresh token thất bại");
        }
    }

    public String getRefreshToken(String email) {
        String token = redisTemplate.opsForValue().get("REFRESH_TOKEN:" + email);
        if (token == null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        return token;
    }

    public String getEmailByRefreshToken(String refreshToken) {
        for (String key : redisTemplate.keys("REFRESH_TOKEN:*")) {
            String storedToken = redisTemplate.opsForValue().get(key);
            if (refreshToken.equals(storedToken)) {
                return key.replace("REFRESH_TOKEN:", "");
            }
        }
        throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    public void deleteRefreshToken(String email) {
        try {
            redisTemplate.delete("REFRESH_TOKEN:" + email);
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "Xóa refresh token thất bại");
        }
    }
}