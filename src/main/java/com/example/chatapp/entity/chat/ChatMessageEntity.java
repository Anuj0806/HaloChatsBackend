package com.example.chatapp.entity.chat;

import com.example.chatapp.entity.enumData.MessageStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(
        name = "chat_messages",
        /*
        A group message fans out into one undelivered row per recipient,
        all sharing the sender's original messageId - the client needs
        that id to de-duplicate and to match acks. So messageId alone
        can no longer be unique; the pair (messageId, receiver) is.

        NOTE FOR EXISTING DATABASES: ddl-auto=update adds constraints
        but never drops them, so the old single-column unique index on
        message_id will survive an upgrade and reject the second
        recipient of every group message. Run the statement in
        src/main/resources/db/migration/V2__group_chat.sql once.
        */
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_message_recipient",
                columnNames = {"message_id", "receiver"}
        ),
        indexes = @Index(name = "idx_chat_messages_receiver_status", columnList = "receiver, status")
)
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String sender;

    /** For a group message, the individual member this copy is for. */
    @Column(nullable = false)
    private String receiver;

    /**
     * Null for a one-to-one message. Set to the group's public id when
     * this copy belongs to a group thread, so the recipient's client
     * files it under the group rather than under the sender.
     */
    @Column(name = "group_id", length = 64)
    private String groupId;

    /**
     * Denormalised so a group member can render "Mira: see you there"
     * without a lookup for a sender they may not have in contacts.
     */
    @Column(name = "sender_name")
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    /**
     * Simple "emoji:sender,emoji:sender" list of reactions on this message.
     * Kept as a flat string (rather than a child table) to stay lightweight
     * for a chat message that is usually deleted shortly after delivery ack.
     */
    @Column(columnDefinition = "TEXT")
    private String reactions;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "send_at", nullable = false)
    private long sendAt;
}
