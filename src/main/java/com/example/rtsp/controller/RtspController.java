package com.example.rtsp.controller;

import com.example.rtsp.service.RtspService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rtsp")
public class RtspController {

    @Autowired
    private RtspService rtspService;

    @GetMapping("/")
    public ResponseEntity<?> getRtspLinks() {
        return ResponseEntity.ok().body(rtspService.getRtspLinks());
    }
    
    @PostMapping("/save")
    public ResponseEntity<?> saveRtspLink(@RequestParam String rtspName, @RequestParam String rtspUrl) {
        return rtspService.saveRtspLink(rtspName, rtspUrl);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRtspLink(@RequestParam String rtspName) {
        return rtspService.deleteRtspLink(rtspName);
    }

    @PostMapping("/stream")
    public ResponseEntity<?> startStream(@RequestParam String rtspName) {
        return ResponseEntity.ok().body(rtspService.startStream(rtspName));   
    }

    @PostMapping("/stop")
    public void stopStream(@RequestParam Long id) {
        rtspService.stopStream(id);
    }
}
