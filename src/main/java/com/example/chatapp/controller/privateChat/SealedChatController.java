package com.example.chatapp.controller.privateChat;

import com.example.chatapp.DTO.privateChat.SealedMessageDTO;
import com.example.chatapp.config.onlineTrackerPublic.OnlineUserTrackerPublic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Relay for sealed (end-to-end encrypted) chat.
 *
 * This class replaces an earlier WebSocketController that had three
 * problems worth naming, because the fixes are the whole design here:
 *
 *  1. It decrypted every message server-side with an AES key hardcoded
 *     into both the backend and the frontend bundle. Anyone with the
 *     repo - or the shipped JS - could read every "private" message.
 *     Nothing is decrypted here; the payload is opaque base64 and the
 *     server has no key material at all.
 *
 *  2. It used AES/ECB, which leaks structure: identical plaintext
 *     blocks produce identical ciphertext blocks. The client now uses
 *     AES-GCM with a per-message nonce, which is authenticated as well
 *     as confidential.
 *
 *  3. It published to /topic/private.{phone}. Topics are public in a
 *     simple broker, so any connected client could subscribe to
 *     another person's topic and receive their messages. Everything
 *     below goes to a per-user queue instead, which the broker only
 *     delivers to the session that owns it.
 *
 * Sealed messages are never persisted. If the recipient is offline the
 * message is dropped and the sender is told - which is the honest
 * behaviour for a conversation that is meant to leave no trace.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SealedChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sealed.send")
    public void send(SealedMessageDTO message, Principal principal) {
        if (principal == null) {
            return;
        }

        // The sender is whoever the socket is authenticated as, never
        // whatever the body claims - otherwise anyone could send a
        // message that appears to come from someone else.
        String sender = principal.getName();
        message.setSender(sender);

        String receiver = message.getReceiver();

        if (receiver == null || receiver.isBlank() || receiver.equals(sender)) {
            return;
        }

        if (!OnlineUserTrackerPublic.isOnlineInPublic(receiver)) {
            // No store-and-forward: a sealed message the server can't
            // read is also one it shouldn't hold. Tell the sender so the
            // UI can say "not delivered" rather than silently losing it.
            messagingTemplate.convertAndSendToUser(
                    sender,
                    "/queue/sealed.undelivered",
                    Map.of("messageId", message.getMessageId(), "receiver", receiver));
            return;
        }

        messagingTemplate.convertAndSendToUser(receiver, "/queue/sealed.receive", message);

        messagingTemplate.convertAndSendToUser(
                sender,
                "/queue/sealed.ack",
                Map.of("messageId", message.getMessageId(), "status", "DELIVERED"));
    }

    /**
     * Typing signal. Carries no content, so there is nothing to encrypt -
     * but it is still routed to one user rather than broadcast.
     */
    @MessageMapping("/sealed.typing")
    public void typing(Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            return;
        }

        Object receiver = payload.get("receiver");
        if (receiver == null) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver),
                "/queue/sealed.typing",
                Map.of("sender", principal.getName(), "typing", payload.getOrDefault("typing", false)));
    }

    /**
     * Tells the other side the conversation was closed, so both devices
     * can clear it at the same moment rather than one copy lingering.
     */
    @MessageMapping("/sealed.close")
    public void close(Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            return;
        }

        Object receiver = payload.get("receiver");
        if (receiver == null) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver),
                "/queue/sealed.closed",
                Map.of("sender", principal.getName()));
    }
}
