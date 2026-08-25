package com.example.chatapp.DTO.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step 3 of the forgot-password flow.
 *
 * The resetToken is what makes this safe: without it, anyone who knew an
 * email address could POST a new password. The token is only ever handed
 * out by {@code /api/auth/forgot-password/verify} after a valid OTP, is
 * scoped to this one email, and expires in minutes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Reset token is required")
    private String resetToken;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
