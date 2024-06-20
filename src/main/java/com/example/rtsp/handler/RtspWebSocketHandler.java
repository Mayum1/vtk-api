package com.example.rtsp.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.rtsp.repository.RtspLinkRepository;
import com.example.rtsp.service.RtspService;

public class RtspWebSocketHandler extends TextWebSocketHandler {

    private final RtspService rtspService;
    private final RtspLinkRepository rtspLinkRepository;
    private Map<WebSocketSession, String> sessions = new HashMap<>();

    public RtspWebSocketHandler(RtspService rtspService, RtspLinkRepository rtspLinkRepository) {
        this.rtspService = rtspService;
        this.rtspLinkRepository = rtspLinkRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String name = getNameFromSession(session);
        sessions.put(session, name);
        // Дополнительная логика при установлении соединения
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Обработка текстового сообщения
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionName = sessions.get(session);
        sessions.remove(session);
        if (!sessions.containsValue(sessionName)) {
            Long id = getIdFromSession(session);
            rtspService.stopStream(id);
        }
    }

    private String getNameFromSession(WebSocketSession session) {
        return session.getUri().getQuery().split("rtspName=")[1].split("&")[0];
    }

    private Long getIdFromSession(WebSocketSession session) {
        String rtspName = getNameFromSession(session);
        return rtspLinkRepository.findByName(rtspName).getId();
    }
}
