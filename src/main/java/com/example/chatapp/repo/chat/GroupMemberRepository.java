package com.example.chatapp.repo.chat;

import com.example.chatapp.entity.chat.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(String groupId);

    Optional<GroupMember> findByGroupIdAndMemberPhone(String groupId, String memberPhone);

    boolean existsByGroupIdAndMemberPhone(String groupId, String memberPhone);

    long countByGroupId(String groupId);

    /** Just the phone numbers - what fan-out actually needs. */
    @Query("SELECT m.memberPhone FROM GroupMember m WHERE m.groupId = :groupId")
    List<String> findMemberPhones(@Param("groupId") String groupId);

    void deleteByGroupIdAndMemberPhone(String groupId, String memberPhone);

    void deleteByGroupId(String groupId);
}
