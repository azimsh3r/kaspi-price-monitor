package com.metaorta.kaspi.service;

import com.metaorta.kaspi.repository.UserRepository;
import com.metaorta.kaspi.security.AuthDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails findByUsername(String username) {
        return new AuthDetails(userRepository.findByUsername(username).orElseThrow());
    }
}
