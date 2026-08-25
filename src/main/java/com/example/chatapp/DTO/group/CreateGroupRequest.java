package com.example.chatapp.DTO.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    @NotBlank(message = "Give the group a name")
    @Size(max = 80, message = "That name is too long")
    private String name;

    @Size(max = 40)
    private String avatarId;

    /**
     * Phone numbers to add, not counting the creator - they're added
     * as admin automatically.
     */
    @NotEmpty(message = "Add at least one other person")
    private List<String> memberPhones;
}
