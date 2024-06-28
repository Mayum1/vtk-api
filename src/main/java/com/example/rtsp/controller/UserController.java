package com.example.rtsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rtsp.dto.UserDTO;
import com.example.rtsp.dto.UserRequest;
import com.example.rtsp.model.User;
import com.example.rtsp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:8000")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Iterable<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping("/signup")
    public User signUp(@RequestBody UserRequest request) {
        return userService.signUp(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody UserRequest request, HttpSession session) {
        return userService.signIn(request, session);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return userService.logout(request, response);
    }

    @PutMapping("/updateRole")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public User updateRole(@RequestBody UserDTO user) {
        return userService.updateRole(user);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public User getUser(@RequestParam String username) {
        return userService.getUser(username);
    }

    @GetMapping("/username")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public String getUsername(HttpSession session) {
        return userService.getUsername(session);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSessionStatus(HttpServletRequest request) {
        return userService.getSessionStatus(request);
    }

    @GetMapping("/isAdmin")
    public boolean isAdmin(HttpSession session) {
        return userService.isAdmin(session);
    }
}
