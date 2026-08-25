package com.example.chatapp.entity.auth.responce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVerify {
    private String name;
    private String email;
    private String phoneNumber;
    private String publicUserId;
}
