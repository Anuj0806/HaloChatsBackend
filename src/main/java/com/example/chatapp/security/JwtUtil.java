package com.example.chatapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    /*
    This used to be:

        private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    - a brand new random key generated every time the JVM starts.
    That means every backend restart silently invalidated every
    signed-in user's session at once: their token was still sitting in
    localStorage, still unexpired by its own 24-hour clock, but
    validateToken() would reject it because it was signed by a key
    that no longer exists anywhere. From the browser this looked like
    a random, unexplained 401 that would then "fix itself" the moment
    someone logged in again and received a token signed by the
    currently running key - exactly the pattern of failing-then-
    working requests this was reported against.

    The fix is a key that survives a restart: read from a property, so
    it's stable across deployments. Development still works with zero
    setup - a random key is generated and logged clearly as a warning
    so the behaviour above doesn't happen silently a second time.
    */
    @Value("${app.security.jwt-secret:}")
    private String configuredSecret;

    private SecretKey key;

    private final long expirationMs = 86_400_000; // 24 hours

    @PostConstruct
    void init() {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            key = Keys.hmacShaKeyFor(random);

            log.warn(
                    "app.security.jwt-secret is not set - using a random signing key for this "
                            + "process only. Every signed-in session will be invalidated the next "
                            + "time this backend restarts. Set app.security.jwt-secret to a long "
                            + "random value (e.g. `openssl rand -base64 64`) before deploying, or "
                            + "before doing anything you don't want to sign everyone out of."
            );
        } else {
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(configuredSecret);
            } catch (IllegalArgumentException notBase64) {
                // Accept a plain string too - not everyone will think to
                // base64-encode it, and HS512 just needs 64+ bytes of
                // entropy, however it arrives.
                decoded = configuredSecret.getBytes(StandardCharsets.UTF_8);
            }

            if (decoded.length < 64) {
                throw new IllegalStateException(
                        "app.security.jwt-secret is too short for HS512 (needs 64+ bytes / 512 bits "
                                + "once decoded). Generate one with: openssl rand -base64 64"
                );
            }

            key = Keys.hmacShaKeyFor(decoded);
        }
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        token = token.replace("Bearer ", "");

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("email", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
