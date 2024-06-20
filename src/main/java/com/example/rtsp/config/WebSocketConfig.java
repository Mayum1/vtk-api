package com.example.rtsp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.example.rtsp.handler.RtspWebSocketHandler;
import com.example.rtsp.repository.RtspLinkRepository;
import com.example.rtsp.service.RtspService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private RtspService rtspService;

    @Autowired
    private RtspLinkRepository rtspLinkRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new RtspWebSocketHandler(rtspService, rtspLinkRepository), "/ws/stream")
            .addInterceptors(new HttpSessionHandshakeInterceptor())
            .setAllowedOrigins("*");
    }

    @Bean
    public RtspWebSocketHandler rtspWebSocketHandler() {
        return new RtspWebSocketHandler(rtspService, rtspLinkRepository);
    }
}
