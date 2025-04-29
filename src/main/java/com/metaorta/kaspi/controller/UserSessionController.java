package com.metaorta.kaspi.controller;

import com.metaorta.kaspi.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserSessionController {
    private final UserSessionService userSessionService;

    @Autowired
    public UserSessionController(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    @GetMapping("/get-session")
    public String getSession(@RequestParam String key) {
        return userSessionService.getUserSessionIdFromRedis(key);
    }
}
