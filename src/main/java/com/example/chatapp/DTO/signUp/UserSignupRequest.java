package com.example.chatapp.DTO.signUp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String city;

    String otp;

    // The user picks this themselves now - the backend no longer generates
    // and emails a password. Confirm-password matching is a frontend-only
    // concern; the backend only ever sees the one final value.
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}