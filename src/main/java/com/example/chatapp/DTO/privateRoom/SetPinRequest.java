package com.example.chatapp.DTO.privateRoom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetPinRequest {

    /** Required only when the account already has a PIN - proves it's really them changing it. */
    private String currentPin;

    @NotBlank(message = "Enter a 4-digit PIN")
    @Pattern(regexp = "\\d{4}", message = "The PIN must be exactly 4 digits")
    private String newPin;
}
