package com.example.chatapp.controller.publicChat;

import com.example.chatapp.DTO.publicChat.PublicChatDTO;
import com.example.chatapp.DTO.publicChat.ReactionDTO;
import com.example.chatapp.DTO.publicChat.ReadReceiptDTO;
import com.example.chatapp.DTO.publicChat.TypingDTO;
import com.example.chatapp.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Thin STOMP entry points for the persisted ("public") chat feature.
 * All business logic lives in {@link ChatService}.
 */
@Controller
@RequiredArgsConstructor
public class PublicChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.ready")
    public void userReady(Principal principal) {
        chatService.deliverPendingMessages(principal.getName());
    }

    @MessageMapping("/messageDelivered")
    public void messageDelivered(String messageId, Principal principal) {
        // The principal is the authoritative identity here - trusting a
        // receiver sent in the body would let any client purge another
        // member's undelivered copy.
        chatService.acknowledgeDelivered(messageId, principal == null ? null : principal.getName());
    }

    @MessageMapping("/chat.send")
    public void sendMessage(PublicChatDTO chatMessage) {
        chatService.sendMessage(chatMessage);
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingDTO typingDTO) {
        chatService.notifyTyping(typingDTO);
    }

    @MessageMapping("/chat.read")
    public void read(ReadReceiptDTO readReceiptDTO) {
        chatService.markAsRead(readReceiptDTO);
    }

    @MessageMapping("/chat.reaction")
    public void reaction(ReactionDTO reactionDTO) {
        chatService.addReaction(reactionDTO);
    }
}
