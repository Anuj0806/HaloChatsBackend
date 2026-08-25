package com.example.chatapp.service.privateChat.impl;

import com.example.chatapp.DTO.privateChat.PublicKeyResponse;
import com.example.chatapp.entity.chat.UserPublicKey;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.chat.UserPublicKeyRepository;
import com.example.chatapp.service.privateChat.KeyDirectoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyDirectoryServiceImpl implements KeyDirectoryService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    private final UserPublicKeyRepository keyRepository;

    @Override
    @Transactional
    public PublicKeyResponse publish(String phone, String publicKeyJwk) {
        String key = publicKeyJwk == null ? "" : publicKeyJwk.trim();

        if (key.isEmpty()) {
            throw new ApiException("Public key is required");
        }

        // Cheap sanity check. A full JWK parse belongs in the client;
        // this just stops obviously wrong content being stored and
        // handed to someone else's browser as a key.
        if (!key.startsWith("{") || !key.contains("\"kty\"")) {
            throw new ApiException("That doesn't look like a JWK public key");
        }

        if (key.contains("\"d\"")) {
            // "d" is the private scalar. If it's here, the client has a
            // serious bug and is about to publish its private key.
            log.error("Refused a key upload containing a private component for {}", phone);
            throw new ApiException("That key contains a private component and was rejected");
        }

        String digest = fingerprint(key);

        // One atomic statement - see UserPublicKeyRepository.upsert for
        // why this replaced a separate find-then-save.
        keyRepository.upsert(phone, key, digest);

        UserPublicKey saved = keyRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalStateException(
                        "Key upsert reported success but the row can't be read back for " + phone));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicKeyResponse find(String phone) {
        return keyRepository.findByPhone(phone)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(
                        "That person hasn't set up sealed chat yet", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicKeyResponse> findAll(List<String> phones) {
        if (phones == null || phones.isEmpty()) {
            return List.of();
        }

        return keyRepository.findByPhoneIn(phones).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Grouped hex, the way secure-messaging apps present safety numbers -
     * a wall of 64 characters is not something anyone reads aloud
     * accurately, and this check only works if people actually do it.
     */
    private String fingerprint(String publicKeyJwk) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKeyJwk.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash).toUpperCase();

            StringBuilder grouped = new StringBuilder();
            for (int i = 0; i < 40; i += 5) {
                if (i > 0) grouped.append(' ');
                grouped.append(hex, i, i + 5);
            }

            return grouped.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private PublicKeyResponse toResponse(UserPublicKey record) {
        return PublicKeyResponse.builder()
                .phone(record.getPhone())
                .publicKey(record.getPublicKey())
                .fingerprint(record.getFingerprint())
                .updatedAt(record.getUpdatedAt() == null ? null : record.getUpdatedAt().format(STAMP))
                .build();
    }
}
