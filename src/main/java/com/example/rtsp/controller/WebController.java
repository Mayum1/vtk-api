package com.example.rtsp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/rtsp")
    public String rtsp() {
        return "rtsp";
    }

    @GetMapping("/csv")
    public String csv() {
        return "csv";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
