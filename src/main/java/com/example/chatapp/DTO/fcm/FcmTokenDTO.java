package com.example.chatapp.DTO.fcm;

import lombok.Data;

@Data
public class FcmTokenDTO {
    private String publicUserId;
    private String token;
    private String deviceId;
    private String platform;
}
