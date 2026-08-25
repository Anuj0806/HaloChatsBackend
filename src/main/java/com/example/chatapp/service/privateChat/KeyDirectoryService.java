package com.example.chatapp.service.privateChat;

import com.example.chatapp.DTO.privateChat.PublicKeyResponse;

import java.util.List;

/**
 * Publishes and serves ECDH public keys for sealed chats.
 *
 * This directory is the one part of sealed chat the server could
 * attack: hand each side a key it controls and it can read everything.
 * Nothing here can prevent that - only the users comparing
 * fingerprints out of band can. The API therefore always returns the
 * fingerprint alongside the key so the client can surface it.
 */
public interface KeyDirectoryService {

    /** Publish or replace the caller's own public key. */
    PublicKeyResponse publish(String phone, String publicKeyJwk);

    /** Look up one person's key. Empty if they've never opened a sealed chat. */
    PublicKeyResponse find(String phone);

    List<PublicKeyResponse> findAll(List<String> phones);
}
