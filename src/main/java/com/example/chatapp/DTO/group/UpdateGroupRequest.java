package com.example.chatapp.DTO.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupRequest {

    @NotBlank(message = "Give the group a name")
    @Size(max = 80, message = "That name is too long")
    private String name;

    @Size(max = 40)
    private String avatarId;
}
