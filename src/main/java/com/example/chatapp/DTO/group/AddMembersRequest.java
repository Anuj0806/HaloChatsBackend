package com.example.chatapp.DTO.group;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMembersRequest {

    @NotEmpty(message = "Pick at least one person to add")
    private List<String> memberPhones;
}
