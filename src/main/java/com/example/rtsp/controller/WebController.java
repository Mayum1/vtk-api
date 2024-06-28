package com.example.rtsp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/rtsp")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RTSP')")
    public String rtsp() {
        return "rtsp";
    }

    @GetMapping("/csv")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_CSV')")
    public String csv() {
        return "csv";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public String admin() {
        return "admin";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
