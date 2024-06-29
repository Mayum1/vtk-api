package com.example.rtsp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.rtsp.dto.UserDTO;
import com.example.rtsp.dto.UserRequest;
import com.example.rtsp.model.User;
import com.example.rtsp.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<?> signUp(UserRequest registrationRequest) {
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return ResponseEntity.ok("Registration successful");
    }

    public ResponseEntity<?> signIn(UserRequest loginRequest, HttpSession session) {
        try {
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            Authentication auth = authenticationManager.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            session.setAttribute("username", loginRequest.getUsername());

            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logout successful");
    }

    public ResponseEntity<?> updateRole(UserDTO updatedUser) {
        Optional<User> optionalUser = userRepository.findById(updatedUser.getId());

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setRole(updatedUser.getRole());
            userRepository.save(existingUser);
            
            return ResponseEntity.ok("Role updated");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    public ResponseEntity<?> getSessionStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");
        }
        return ResponseEntity.ok("Session active");
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username).get();
    }

    public String getUsername(HttpSession session) {
        return (String) session.getAttribute("username");
    }

    public boolean isAdmin(HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = userRepository.findByUsername(username).get();
        String role = user.getRole();
        return role.contains("ROLE_ADMIN");
    }
}
