package com.example.chatapp.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step 2 of the forgot-password flow: prove you own the mailbox.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Enter the code we emailed you")
    @Size(min = 6, max = 6, message = "The code is 6 digits")
    private String otp;
}
