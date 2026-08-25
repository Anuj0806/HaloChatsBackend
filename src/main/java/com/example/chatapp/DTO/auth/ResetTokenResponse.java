package com.example.chatapp.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetTokenResponse {

    private String resetToken;

    /** Seconds until the token stops working, so the UI can show a countdown. */
    private long expiresInSeconds;
}
