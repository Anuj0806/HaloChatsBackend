package com.example.chatapp.service.chat;

import com.example.chatapp.DTO.publicChat.PublicChatDTO;
import com.example.chatapp.DTO.publicChat.ReactionDTO;
import com.example.chatapp.DTO.publicChat.ReadReceiptDTO;
import com.example.chatapp.DTO.publicChat.TypingDTO;

/**
 * Business logic for the "public" (persisted) chat feature. Kept separate
 * from PublicChatController so the controller only deals with STOMP
 * plumbing and this service owns persistence + delivery rules.
 */
public interface ChatService {

    /** Persist a message and deliver it (live push, or FCM if offline). */
    void sendMessage(PublicChatDTO message);

    /** Push every message queued for a user once their client says it's ready. */
    void deliverPendingMessages(String username);

    /**
     * Called once a client has durably stored a message - that
     * recipient's server-side copy can be purged. Scoped by receiver
     * because a group message has one copy per member.
     */
    void acknowledgeDelivered(String messageId, String receiver);

    /** Mark a message read and notify the original sender (read receipt / blue ticks). */
    void markAsRead(ReadReceiptDTO receipt);

    /** Attach an emoji reaction to a message and notify the other participant. */
    void addReaction(ReactionDTO reaction);

    /** Relay a "user is typing" signal to the other participant. */
    void notifyTyping(TypingDTO typing);
}
