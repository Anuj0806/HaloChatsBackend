package com.example.chatapp.DTO.privateChat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A sealed message in transit.
 *
 * Note what is NOT here: any field the server could read. `ciphertext`
 * and `iv` are opaque base64; the server relays them untouched and
 * stores nothing. Sender and receiver have to be visible because the
 * broker needs them to route - sealed chat hides message content, not
 * the fact that two people are talking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SealedMessageDTO {

    private String messageId;
    private String sender;
    private String receiver;

    /** Base64 AES-GCM ciphertext. Opaque to this server. */
    private String ciphertext;

    /** Base64 96-bit nonce, fresh for every message. */
    private String iv;

    private long timeStamp;

    /**
     * Fingerprint of the key the sender encrypted with. Lets the
     * recipient notice immediately if it isn't the key they verified.
     */
    private String senderFingerprint;
}
