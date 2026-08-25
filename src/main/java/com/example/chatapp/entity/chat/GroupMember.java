package com.example.chatapp.entity.chat;

import com.example.chatapp.entity.enumData.GroupRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Membership is its own table rather than a collection on ChatGroup.
 * Fan-out reads "who is in this group" on every single message, and a
 * plain indexed row per member keeps that a single cheap query.
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "group_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_member",
                columnNames = {"group_id", "member_phone"}
        ),
        indexes = {
                @Index(name = "idx_group_members_group", columnList = "group_id"),
                @Index(name = "idx_group_members_phone", columnList = "member_phone")
        }
)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "member_phone", nullable = false)
    private String memberPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = GroupRole.MEMBER;
        }
    }
}
