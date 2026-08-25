package com.example.chatapp.DTO.publicChat;

import lombok.Data;

@Data
public class AckDTO {
    public static final int SENT = 1;
    public static final int DELIVERED = 2;
    public static final int READ = 3;

    private String messageId;
    private int status;
    private String info;
}
