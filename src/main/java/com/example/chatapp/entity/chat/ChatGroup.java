package com.example.chatapp.entity.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_groups")
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The public identifier. Clients use this as the conversation's
     * chat id, so a group thread slots into the same local storage
     * shape as a one-to-one thread with no special casing.
     */
    @Column(name = "group_id", nullable = false, unique = true, length = 64)
    private String groupId;

    @Column(nullable = false, length = 80)
    private String name;

    /** One of the generated avatar style ids, same set as users. */
    @Column(name = "avatar_id", length = 40)
    private String avatarId;

    /** Phone number of whoever created it. */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
