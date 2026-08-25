package com.example.chatapp.service.group;

import com.example.chatapp.DTO.group.AddMembersRequest;
import com.example.chatapp.DTO.group.CreateGroupRequest;
import com.example.chatapp.DTO.group.GroupResponse;
import com.example.chatapp.DTO.group.UpdateGroupRequest;

import java.util.List;

/**
 * Group lifecycle and membership.
 *
 * Every method takes the acting user's phone number and enforces
 * permission itself rather than trusting the controller to have
 * checked. A missing check in one endpoint would otherwise let anyone
 * who knows a groupId rename someone else's group or read its roster.
 */
public interface GroupService {

    GroupResponse createGroup(String actorPhone, CreateGroupRequest request);

    /** Every group the actor belongs to. */
    List<GroupResponse> listGroups(String actorPhone);

    /** Members only. */
    GroupResponse getGroup(String actorPhone, String groupId);

    /** Admins only. */
    GroupResponse updateGroup(String actorPhone, String groupId, UpdateGroupRequest request);

    /** Admins only. */
    GroupResponse addMembers(String actorPhone, String groupId, AddMembersRequest request);

    /**
     * Admins may remove anyone; anyone may remove themselves (leaving).
     * Removing the last admin promotes the longest-standing member so a
     * group can't become permanently unmanageable.
     */
    GroupResponse removeMember(String actorPhone, String groupId, String targetPhone);

    /** Phone numbers to fan a message out to. Empty if the group is gone. */
    List<String> memberPhones(String groupId);

    boolean isMember(String groupId, String phone);
}
