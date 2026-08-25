package com.example.chatapp.controller.group;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.group.AddMembersRequest;
import com.example.chatapp.DTO.group.CreateGroupRequest;
import com.example.chatapp.DTO.group.GroupResponse;
import com.example.chatapp.DTO.group.UpdateGroupRequest;
import com.example.chatapp.security.CurrentUserResolver;
import com.example.chatapp.service.group.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final CurrentUserResolver currentUser;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateGroupRequest request) {

        String phone = currentUser.phoneOf(authorization);
        GroupResponse group = groupService.createGroup(phone, request);

        return ResponseEntity.ok(ApiResponse.ok("Group created", group));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> list(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String phone = currentUser.phoneOf(authorization);
        List<GroupResponse> groups = groupService.listGroups(phone);

        return ResponseEntity.ok(ApiResponse.ok("Groups loaded", groups));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> get(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String groupId) {

        String phone = currentUser.phoneOf(authorization);

        return ResponseEntity.ok(ApiResponse.ok("Group loaded", groupService.getGroup(phone, groupId)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String groupId,
            @Valid @RequestBody UpdateGroupRequest request) {

        String phone = currentUser.phoneOf(authorization);

        return ResponseEntity.ok(
                ApiResponse.ok("Group updated", groupService.updateGroup(phone, groupId, request)));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupResponse>> addMembers(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String groupId,
            @Valid @RequestBody AddMembersRequest request) {

        String phone = currentUser.phoneOf(authorization);

        return ResponseEntity.ok(
                ApiResponse.ok("Members added", groupService.addMembers(phone, groupId, request)));
    }

    /** Also how someone leaves: pass their own number. */
    @DeleteMapping("/{groupId}/members/{memberPhone}")
    public ResponseEntity<ApiResponse<GroupResponse>> removeMember(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String groupId,
            @PathVariable String memberPhone) {

        String phone = currentUser.phoneOf(authorization);
        GroupResponse group = groupService.removeMember(phone, groupId, memberPhone);

        String message = phone.equals(memberPhone) ? "You left the group" : "Member removed";

        return ResponseEntity.ok(ApiResponse.ok(message, group));
    }
}
