package com.example.chatapp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The public view of an account - what other users are allowed to see when
 * they look someone up by phone number. Deliberately excludes the password
 * hash, city, verification flag and internal id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String name;
    private String phone;
    private String email;
    private String avatarId;
    private String about;
}
