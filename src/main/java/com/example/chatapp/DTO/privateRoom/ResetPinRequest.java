package com.example.chatapp.DTO.privateRoom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Recovers a forgotten Private Room PIN using the one credential that
 * outranks it: the account password. Without this, forgetting a
 * 4-digit PIN would permanently lock someone out of their own
 * Private Room with no way back in - a dead end this app shouldn't
 * have.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPinRequest {

    @NotBlank(message = "Enter your account password")
    private String accountPassword;

    @NotBlank(message = "Choose a 4-digit PIN")
    @Pattern(regexp = "\\d{4}", message = "The PIN must be exactly 4 digits")
    private String newPin;
}
