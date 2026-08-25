package com.example.chatapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "signup")
public class UserSignup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_user_id", unique = true, nullable = false, updatable = false)
    private String publicUserId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String city;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    /**
     * Which generated avatar this account shows. Not an uploaded image -
     * the frontend draws these from a fixed set of gradient/pattern styles,
     * so all that has to travel is a short style id like "orbit-3".
     * Null means "derive one from the phone number", which is what every
     * account created before this column existed will do.
     */
    @Column(name = "avatar_id", length = 40)
    private String avatarId;

    /** The short status line shown under the name. */
    @Column(name = "about", length = 140)
    private String about;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    private void generatePublicId() {
        if (this.publicUserId == null) {
            this.publicUserId = UUID.randomUUID().toString();
        }
    }
}
