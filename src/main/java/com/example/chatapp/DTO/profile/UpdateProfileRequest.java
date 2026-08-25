package com.example.chatapp.DTO.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 60, message = "Name is too long")
    private String name;

    @Size(max = 140, message = "Keep your status under 140 characters")
    private String about;

    /** One of the generated avatar style ids the frontend offers. */
    @Size(max = 40)
    private String avatarId;

    @Size(max = 80)
    private String city;
}
