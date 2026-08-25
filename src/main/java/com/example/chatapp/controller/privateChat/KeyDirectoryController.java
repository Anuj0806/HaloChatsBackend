package com.example.chatapp.controller.privateChat;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.privateChat.PublicKeyResponse;
import com.example.chatapp.DTO.privateChat.PublishKeyRequest;
import com.example.chatapp.security.CurrentUserResolver;
import com.example.chatapp.service.privateChat.KeyDirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keys")
public class KeyDirectoryController {

    private final KeyDirectoryService keyDirectoryService;
    private final CurrentUserResolver currentUser;

    /**
     * Publish the caller's own public key.
     *
     * The phone number comes from the token, never the body - otherwise
     * anyone could overwrite someone else's key with their own and read
     * that person's sealed chats.
     */
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<PublicKeyResponse>> publish(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody PublishKeyRequest request) {

        String phone = currentUser.phoneOf(authorization);
        PublicKeyResponse published = keyDirectoryService.publish(phone, request.getPublicKey());

        return ResponseEntity.ok(ApiResponse.ok("Key published", published));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PublicKeyResponse>> mine(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String phone = currentUser.phoneOf(authorization);

        return ResponseEntity.ok(ApiResponse.ok("Key loaded", keyDirectoryService.find(phone)));
    }

    /** Fetch someone's key so a sealed chat can be set up with them. */
    @GetMapping("/{phone}")
    public ResponseEntity<ApiResponse<PublicKeyResponse>> lookup(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String phone) {

        // Requires a valid session: the directory shouldn't be an open
        // endpoint for enumerating who uses sealed chat.
        currentUser.phoneOf(authorization);

        return ResponseEntity.ok(ApiResponse.ok("Key loaded", keyDirectoryService.find(phone)));
    }
}
