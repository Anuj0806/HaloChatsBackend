package com.example.chatapp.DTO.publicChat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicChatDTO {

    private String messageId;
    private String sender;

    /**
     * The individual recipient. For a group message the client leaves
     * this null on the way in - the server fills it per copy on the
     * way out, so each member's client sees itself as the receiver.
     */
    private String receiver;

    private String payload;
    private long timeStamp;

    /**
     * Set for a group message. When present the recipient files the
     * message under the group rather than under the sender, and
     * `receiver` is that particular member.
     */
    private String groupId;

    /**
     * Who sent it, by name. Carried on the wire so a group member can
     * see "Mira: on my way" for someone they've never had a one-to-one
     * chat with and therefore can't look up locally.
     */
    private String senderName;

    /** Convenience constructor for the one-to-one path. */
    public PublicChatDTO(String messageId, String sender, String receiver, String payload, long timeStamp) {
        this(messageId, sender, receiver, payload, timeStamp, null, null);
    }
}
