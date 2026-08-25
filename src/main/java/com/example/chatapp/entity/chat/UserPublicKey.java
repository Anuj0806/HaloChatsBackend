package com.example.chatapp.entity.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A user's published ECDH public key, used to set up sealed chats.
 *
 * Only the PUBLIC half is ever sent here. The private key is generated
 * in the browser and never leaves it, which is what makes the sealed
 * chat actually end-to-end: this server can relay ciphertext but has
 * no way to read it.
 *
 * One key per device is not modelled. This is a single-key-per-account
 * design, so signing in on a second browser replaces the key and older
 * sealed conversations can no longer be decrypted there. That is a real
 * limitation, documented in the README rather than hidden.
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_public_keys")
public class UserPublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phone;

    /** The public key as a JWK JSON string, exactly as the browser exported it. */
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    /**
     * SHA-256 of the key, formatted for humans. Both parties compare
     * this out of band to detect a substituted key - without that check
     * a malicious server could hand each side its own key and read
     * everything.
     */
    @Column(nullable = false, length = 128)
    private String fingerprint;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
