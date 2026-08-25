package com.example.chatapp.config.onlineTrackerPrivate;

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
public class WebSocketEventListenerPrivate {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @EventListener
    public void handleConnectPrivate(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            OnlineUserTracker.userOnline(username, sessionId);
            log.info("User ONLINE: {} | Session: {}", username, sessionId);
        }
    }

    @EventListener
    public void handleDisconnectPrivate(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        OnlineUserTracker.userOffline(sessionId);
        log.info("Session OFFLINE: {}", sessionId);
    }
}
