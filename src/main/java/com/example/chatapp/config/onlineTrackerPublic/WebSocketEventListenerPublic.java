package com.example.chatapp.config.onlineTrackerPublic;

import com.example.chatapp.repo.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListenerPublic {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @EventListener
    public void handleConnectPublic(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            OnlineUserTrackerPublic.userOnlineInPublic(username, sessionId);
            log.info("User ONLINE: {} | Session: {}", username, sessionId);
        }
    }

    @EventListener
    public void handleDisconnectPublic(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        OnlineUserTrackerPublic.userOfflineInPublic(sessionId);
        log.info("Session OFFLINE: {}", sessionId);
    }
}
