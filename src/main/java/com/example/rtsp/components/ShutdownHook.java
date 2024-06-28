package com.example.rtsp.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.rtsp.service.RtspService;

import jakarta.annotation.PreDestroy;

@Component
public class ShutdownHook {
  @Autowired
	private RtspService rtspService;

    @PreDestroy
    public void onShutdown() {
      rtspService.stopAllStreams();
    }
}
