package com.example.chatapp.model.privateChat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @JsonIgnore
    private int id;

    private String chatId;
    private String sender;
    private String senderName;
    private String text;
    private Long timestamp;
    private Boolean isGroup;
    private List<String> members;

    @JsonIgnore
    private  String status;
}
