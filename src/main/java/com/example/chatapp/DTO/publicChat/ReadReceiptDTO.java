package com.example.chatapp.DTO.publicChat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptDTO {
    private String messageId;
    private String sender;
    private String reader;
}
