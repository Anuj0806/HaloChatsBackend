package com.example.chatapp.DTO.publicChat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingDTO {

    private String sender;

    /** Null for a group signal, which goes to every other member. */
    private String receiver;

    private boolean typing;

    private String groupId;

    public TypingDTO(String sender, String receiver, boolean typing) {
        this(sender, receiver, typing, null);
    }
}
