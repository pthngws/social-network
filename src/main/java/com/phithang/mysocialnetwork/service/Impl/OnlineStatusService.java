package com.phithang.mysocialnetwork.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OnlineStatusService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    // Ping => Cập nhật lastSeen
    public void ping(Long userId) {
        String lastSeenKey = "user:" + userId + ":lastSeen";
        long currentTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(lastSeenKey, String.valueOf(currentTime));
    }

    // Check online
    public boolean isUserOnline(Long userId) {
        Long lastSeen = getLastSeen(userId);
        if (lastSeen == null) return false;

        long now = System.currentTimeMillis();
        long diffMillis = now - lastSeen;

        return diffMillis <= 60 * 1000; // Nếu cách hiện tại <= 60 giây => online
    }

    // Lấy last seen millis
    public Long getLastSeen(Long userId) {
        String key = "user:" + userId + ":lastSeen";
        String value = redisTemplate.opsForValue().get(key);
        return (value != null) ? Long.parseLong(value) : null;
    }

    public Long getLastSeenMinutesAgo(Long userId) {
        Long lastSeen = getLastSeen(userId);
        if (lastSeen == null) {
            return null; // Không có lastSeen
        }
        long now = System.currentTimeMillis();
        long diffMillis = now - lastSeen;
        return TimeUnit.MILLISECONDS.toMinutes(diffMillis); // Chuyển đổi thành phút
    }
}
