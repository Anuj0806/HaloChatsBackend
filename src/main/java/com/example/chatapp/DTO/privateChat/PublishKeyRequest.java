package com.example.chatapp.DTO.privateChat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishKeyRequest {

    /** The ECDH P-256 public key, JWK-encoded. */
    @NotBlank(message = "Public key is required")
    @Size(max = 2000, message = "That key is too large to be a P-256 public key")
    private String publicKey;
}
