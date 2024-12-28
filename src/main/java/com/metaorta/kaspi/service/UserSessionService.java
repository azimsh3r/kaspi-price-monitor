package com.metaorta.kaspi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public UserSessionService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getUserSessionId(String key) {
        String sessionId = redisTemplate.opsForValue().get(key);
        System.out.println(sessionId);
        return sessionId;

    }

    public void saveSessionToRedis(String username, String sessionId) {
        redisTemplate.opsForValue().set(username, sessionId);
        System.out.println("Session ID for " + username + " saved to Redis.");
    }
}
