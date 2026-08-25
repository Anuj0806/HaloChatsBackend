package com.example.chatapp.DTO.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String userId;
    private String userName;
    private String phone;
    private String token;
}
