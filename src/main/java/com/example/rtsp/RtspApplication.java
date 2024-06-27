package com.example.rtsp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.example.rtsp.service.RtspService;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class RtspApplication {

	public static void main(String[] args) {
		SpringApplication.run(RtspApplication.class, args);
	}

}

@Component
class ShutdownHook {
	@Autowired
	private RtspService rtspService;

    @PreDestroy
    public void onShutdown() {
		rtspService.stopAllStreams();
    }
}
