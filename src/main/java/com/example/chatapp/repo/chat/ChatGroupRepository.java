package com.example.chatapp.repo.chat;

import com.example.chatapp.entity.chat.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    Optional<ChatGroup> findByGroupId(String groupId);

    /** Every group the given phone number belongs to, newest first. */
    @Query("""
            SELECT g FROM ChatGroup g
            WHERE g.groupId IN (
                SELECT m.groupId FROM GroupMember m WHERE m.memberPhone = :phone
            )
            ORDER BY g.createdAt DESC
            """)
    List<ChatGroup> findAllForMember(@Param("phone") String phone);

    void deleteByGroupId(String groupId);
}
