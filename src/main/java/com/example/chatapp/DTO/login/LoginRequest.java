package com.example.chatapp.DTO.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String username; // email OR phone
    private String password;
}
