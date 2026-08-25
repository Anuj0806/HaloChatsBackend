package com.example.chatapp.DTO.privateChat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {
    private String phone;
    private String publicKey;
    private String fingerprint;
    private String updatedAt;
}
