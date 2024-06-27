package com.example.rtsp.controller;

import com.example.rtsp.service.RtspService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rtsp")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RTSP')")
public class RtspController {

    @Autowired
    private RtspService rtspService;

    @GetMapping("/")
    public ResponseEntity<?> getRtspLinks() {
        return ResponseEntity.ok().body(rtspService.getRtspLinks());
    }

    @PostMapping("/")
    public List<String> getRtspLinksByUser(HttpSession session) {
        return rtspService.getRtspLinksByUser(session);
    }
    
    @PostMapping("/save")
    public ResponseEntity<?> saveRtspLink(@RequestParam String rtspName, @RequestParam String rtspUrl, HttpSession session) {
        return rtspService.saveRtspLink(rtspName, rtspUrl, session);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRtspLink(@RequestParam String rtspName, HttpSession session) {
        return rtspService.deleteRtspLink(rtspName, session);
    }

    @PostMapping("/stream")
    public ResponseEntity<?> startStream(@RequestParam String rtspName, HttpSession session) {
        return rtspService.startStream(rtspName, session);
    }

    @PostMapping("/stop")
    public void stopStream(@RequestParam Long id) {
        rtspService.stopStream(id);
    }
}
