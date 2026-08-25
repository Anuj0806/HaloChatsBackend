package com.example.chatapp.DTO.publicChat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    private String messageId;
    private String sender;
    private String receiver;
    private String emoji;
}
