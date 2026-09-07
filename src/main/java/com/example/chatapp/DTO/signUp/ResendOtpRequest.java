package com.example.chatapp.DTO.signUp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResendOtpRequest {
    private String email;

    private String otp;
}
