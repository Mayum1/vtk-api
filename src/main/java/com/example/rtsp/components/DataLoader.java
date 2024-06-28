package com.example.rtsp.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.rtsp.dto.UserRequest;
import com.example.rtsp.repository.UserRepository;
import com.example.rtsp.service.UserService;

import jakarta.transaction.Transactional;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userService.signUp(new UserRequest("admin", "admin"));
            userRepository.findByUsername("admin").get().setRole("ROLE_ADMIN");
        }
    }
}
