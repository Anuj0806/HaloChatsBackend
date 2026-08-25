package com.example.chatapp.entity.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * The four-digit PIN that gates the Private Room tab.
 *
 * This is an access-control gate, not a cryptographic secret. It is
 * NEVER used as key material for sealed chat - a 4-digit PIN has only
 * 10,000 possible values, which would be a catastrophic weakness if
 * it fed into anything encryption-related. Its only job is deciding
 * whether this browser gets to render the Private Room screen; the
 * end-to-end encryption in {@code UserPublicKey} is completely
 * independent of it.
 *
 * Because 10,000 values is small enough to brute-force in seconds
 * without a slow hash and a lockout, both are mandatory here: the PIN
 * is hashed with the same BCrypt encoder used for account passwords,
 * and repeated wrong guesses lock the account out with growing
 * backoff (see PrivateRoomPinServiceImpl).
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "private_room_pins")
public class PrivateRoomPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private int failedAttempts = 0;

    /** Null when not currently locked out. */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
