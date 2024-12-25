package com.metaorta.kaspi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String getUserSessionId(String key) {
        String sessionId = redisTemplate.opsForValue().get(key);
        System.out.println(sessionId);
        return sessionId;

    }
}
