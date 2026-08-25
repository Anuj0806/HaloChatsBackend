package com.example.chatapp.DTO.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The signed-in user's own view of their account - includes a couple of
 * fields ({@code email}, {@code city}, {@code verified}) that the public
 * UserDTO deliberately withholds from other people.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String publicUserId;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String about;
    private String avatarId;
    private boolean verified;
    private String joinedAt;
}
