package com.example.chatapp.DTO.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    private String groupId;
    private String name;
    private String avatarId;
    private String createdBy;
    private String createdAt;

    /** The requesting user's own role, so the UI can hide admin controls. */
    private String myRole;

    private List<GroupMemberResponse> members;
}
