package com.example.chatapp.security;

import com.example.chatapp.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Issues and validates the short-lived token that sits between "I proved I
 * own this mailbox" (OTP verified) and "here is my new password".
 *
 * Two deliberate design choices:
 *
 * 1. The token is a self-contained HMAC, not a database row. There is no
 *    reset_token table to create, migrate or clean up, and a restart can't
 *    strand a user mid-flow.
 *
 * 2. The token is bound to a fingerprint of the account's CURRENT password
 *    hash. The moment the password actually changes, that fingerprint no
 *    longer matches and every token issued against the old one stops
 *    validating. That gives single-use semantics for free - a token can't
 *    be replayed to silently reset the password a second time.
 */
@Component
public class ResetTokenService {

    /** Ten minutes is long enough to type a password, short enough to matter. */
    public static final long VALIDITY_SECONDS = 600;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PURPOSE = "pwd-reset";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secret;

    public ResetTokenService(
            @Value("${app.security.reset-token-secret:}") String configuredSecret) {

        if (configuredSecret == null || configuredSecret.isBlank()) {
            // No secret configured (local dev): generate a strong random one
            // for this process. Tokens then stop working across restarts,
            // which is safe - a user simply requests a fresh code. Set
            // app.security.reset-token-secret in production.
            byte[] generated = new byte[32];
            RANDOM.nextBytes(generated);
            this.secret = generated;
        } else {
            this.secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * @param email           the account the token is scoped to
     * @param currentPasswordHash the hash the token is pinned against
     */
    public String issue(String email, String currentPasswordHash) {
        long expiresAt = System.currentTimeMillis() + (VALIDITY_SECONDS * 1000);
        String payload = buildPayload(email, currentPasswordHash, expiresAt);

        String encodedPayload = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = ENCODER.encodeToString(sign(encodedPayload));

        return encodedPayload + "." + signature;
    }

    /**
     * Throws {@link ApiException} rather than returning a boolean so every
     * caller gets the same message and status without having to remember to
     * check a flag.
     */
    public void validate(String token, String email, String currentPasswordHash) {
        if (token == null || token.isBlank()) {
            throw expired();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw expired();
        }

        byte[] expectedSignature = sign(parts[0]);
        byte[] providedSignature;
        try {
            providedSignature = DECODER.decode(parts[1]);
        } catch (IllegalArgumentException ex) {
            throw expired();
        }

        // Constant-time compare - a timing side channel here would let an
        // attacker forge a signature byte by byte.
        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            throw expired();
        }

        String payload;
        try {
            payload = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw expired();
        }

        String[] fields = payload.split("\\|");
        if (fields.length != 5 || !PURPOSE.equals(fields[0])) {
            throw expired();
        }

        if (!fields[1].equalsIgnoreCase(email)) {
            throw expired();
        }

        if (!fields[2].equals(fingerprint(currentPasswordHash))) {
            // Password already changed since this token was issued.
            throw expired();
        }

        long expiresAt;
        try {
            expiresAt = Long.parseLong(fields[3]);
        } catch (NumberFormatException ex) {
            throw expired();
        }

        if (System.currentTimeMillis() > expiresAt) {
            throw expired();
        }
    }

    private String buildPayload(String email, String currentPasswordHash, long expiresAt) {
        byte[] nonceBytes = new byte[8];
        RANDOM.nextBytes(nonceBytes);
        String nonce = HexFormat.of().formatHex(nonceBytes);

        return String.join("|",
                PURPOSE,
                email.toLowerCase(),
                fingerprint(currentPasswordHash),
                String.valueOf(expiresAt),
                nonce);
    }

    /**
     * A short digest of the password hash. The hash itself never goes into
     * the token - only enough of a fingerprint to detect that it changed.
     */
    private String fingerprint(String passwordHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(
                    String.valueOf(passwordHash).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 16);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private byte[] sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign reset token", ex);
        }
    }

    private ApiException expired() {
        return new ApiException(
                "This reset link has expired. Request a new code to continue.",
                HttpStatus.BAD_REQUEST);
    }
}
