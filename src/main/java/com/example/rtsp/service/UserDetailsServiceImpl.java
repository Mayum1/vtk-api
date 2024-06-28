package com.example.rtsp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.rtsp.model.User;
import com.example.rtsp.model.UserDetailsImpl;
import com.example.rtsp.repository.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(UserDetailsImpl::new)
                .orElseThrow(()->
                    new UsernameNotFoundException(username + "- there is not such user"));
    }
}
