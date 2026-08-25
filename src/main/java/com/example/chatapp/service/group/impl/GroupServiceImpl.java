package com.example.chatapp.service.group.impl;

import com.example.chatapp.DTO.group.AddMembersRequest;
import com.example.chatapp.DTO.group.CreateGroupRequest;
import com.example.chatapp.DTO.group.GroupMemberResponse;
import com.example.chatapp.DTO.group.GroupResponse;
import com.example.chatapp.DTO.group.UpdateGroupRequest;
import com.example.chatapp.entity.chat.ChatGroup;
import com.example.chatapp.entity.chat.GroupMember;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.entity.enumData.GroupRole;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.chat.ChatGroupRepository;
import com.example.chatapp.repo.chat.GroupMemberRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.service.group.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final DateTimeFormatter CREATED_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    /** Keeps fan-out bounded and stops a single POST creating a mailing list. */
    private static final int MAX_MEMBERS = 256;

    private final ChatGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepositorySignup userRepository;

    @Override
    @Transactional
    public GroupResponse createGroup(String actorPhone, CreateGroupRequest request) {
        // LinkedHashSet: de-duplicates a list that named someone twice
        // while keeping the order the user picked them in.
        Set<String> invited = new LinkedHashSet<>();
        request.getMemberPhones().stream()
                .filter(phone -> phone != null && !phone.isBlank())
                .map(String::trim)
                .filter(phone -> !phone.equals(actorPhone))
                .forEach(invited::add);

        if (invited.isEmpty()) {
            throw new ApiException("Add at least one other person to the group.");
        }

        if (invited.size() + 1 > MAX_MEMBERS) {
            throw new ApiException("Groups are limited to " + MAX_MEMBERS + " people.");
        }

        // Only real, verified accounts. Otherwise a group could carry
        // phantom members that never receive anything.
        List<String> unknown = invited.stream()
                .filter(phone -> userRepository.findByPhoneNumber(phone).isEmpty())
                .toList();

        if (!unknown.isEmpty()) {
            throw new ApiException("No Halo account for " + String.join(", ", unknown));
        }

        ChatGroup group = groupRepository.save(ChatGroup.builder()
                .groupId("grp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20))
                .name(request.getName().trim())
                .avatarId(request.getAvatarId())
                .createdBy(actorPhone)
                .build());

        memberRepository.save(GroupMember.builder()
                .groupId(group.getGroupId())
                .memberPhone(actorPhone)
                .role(GroupRole.ADMIN)
                .build());

        invited.forEach(phone -> memberRepository.save(GroupMember.builder()
                .groupId(group.getGroupId())
                .memberPhone(phone)
                .role(GroupRole.MEMBER)
                .build()));

        log.info("Group {} created by {} with {} members", group.getGroupId(), actorPhone, invited.size() + 1);

        return toResponse(group, actorPhone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(String actorPhone) {
        return groupRepository.findAllForMember(actorPhone).stream()
                .map(group -> toResponse(group, actorPhone))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroup(String actorPhone, String groupId) {
        ChatGroup group = requireGroup(groupId);
        requireMember(groupId, actorPhone);

        return toResponse(group, actorPhone);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(String actorPhone, String groupId, UpdateGroupRequest request) {
        ChatGroup group = requireGroup(groupId);
        requireAdmin(groupId, actorPhone);

        group.setName(request.getName().trim());

        if (request.getAvatarId() != null && !request.getAvatarId().isBlank()) {
            group.setAvatarId(request.getAvatarId().trim());
        }

        groupRepository.save(group);

        return toResponse(group, actorPhone);
    }

    @Override
    @Transactional
    public GroupResponse addMembers(String actorPhone, String groupId, AddMembersRequest request) {
        ChatGroup group = requireGroup(groupId);
        requireAdmin(groupId, actorPhone);

        long current = memberRepository.countByGroupId(groupId);

        for (String raw : request.getMemberPhones()) {
            if (raw == null || raw.isBlank()) continue;

            String phone = raw.trim();

            // Silently skipping an existing member is right here: adding
            // someone already present is a no-op, not an error worth
            // failing the whole batch over.
            if (memberRepository.existsByGroupIdAndMemberPhone(groupId, phone)) continue;

            if (userRepository.findByPhoneNumber(phone).isEmpty()) {
                throw new ApiException("No Halo account for " + phone);
            }

            if (current + 1 > MAX_MEMBERS) {
                throw new ApiException("Groups are limited to " + MAX_MEMBERS + " people.");
            }

            memberRepository.save(GroupMember.builder()
                    .groupId(groupId)
                    .memberPhone(phone)
                    .role(GroupRole.MEMBER)
                    .build());

            current += 1;
        }

        return toResponse(group, actorPhone);
    }

    @Override
    @Transactional
    public GroupResponse removeMember(String actorPhone, String groupId, String targetPhone) {
        ChatGroup group = requireGroup(groupId);

        boolean leavingSelf = actorPhone.equals(targetPhone);

        if (!leavingSelf) {
            requireAdmin(groupId, actorPhone);
        } else {
            requireMember(groupId, actorPhone);
        }

        GroupMember target = memberRepository.findByGroupIdAndMemberPhone(groupId, targetPhone)
                .orElseThrow(() -> new ApiException("That person isn't in this group", HttpStatus.NOT_FOUND));

        memberRepository.delete(target);

        List<GroupMember> remaining = memberRepository.findByGroupId(groupId);

        // An empty group is just rows nobody can reach. Delete it.
        if (remaining.isEmpty()) {
            groupRepository.deleteByGroupId(groupId);
            log.info("Group {} deleted - last member left", groupId);

            return GroupResponse.builder()
                    .groupId(groupId)
                    .name(group.getName())
                    .members(List.of())
                    .build();
        }

        // If that was the last admin, promote whoever has been in the
        // group longest. Otherwise the group can never be renamed or
        // have members added again.
        boolean anyAdmin = remaining.stream().anyMatch(m -> m.getRole() == GroupRole.ADMIN);

        if (!anyAdmin) {
            GroupMember successor = remaining.stream()
                    .min(Comparator.comparing(GroupMember::getJoinedAt))
                    .orElseThrow();

            successor.setRole(GroupRole.ADMIN);
            memberRepository.save(successor);

            log.info("Promoted {} to admin of {} after last admin left", successor.getMemberPhone(), groupId);
        }

        return toResponse(group, actorPhone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> memberPhones(String groupId) {
        return memberRepository.findMemberPhones(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(String groupId, String phone) {
        return memberRepository.existsByGroupIdAndMemberPhone(groupId, phone);
    }

    /* ---------- helpers ---------- */

    private ChatGroup requireGroup(String groupId) {
        return groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new ApiException("That group no longer exists", HttpStatus.NOT_FOUND));
    }

    private GroupMember requireMember(String groupId, String phone) {
        return memberRepository.findByGroupIdAndMemberPhone(groupId, phone)
                // 404 rather than 403: someone who isn't in a group
                // shouldn't be able to confirm it exists at all.
                .orElseThrow(() -> new ApiException("That group no longer exists", HttpStatus.NOT_FOUND));
    }

    private void requireAdmin(String groupId, String phone) {
        GroupMember member = requireMember(groupId, phone);

        if (member.getRole() != GroupRole.ADMIN) {
            throw new ApiException("Only group admins can do that", HttpStatus.FORBIDDEN);
        }
    }

    private GroupResponse toResponse(ChatGroup group, String actorPhone) {
        List<GroupMember> members = memberRepository.findByGroupId(group.getGroupId());

        List<String> phones = members.stream().map(GroupMember::getMemberPhone).toList();

        // One query scoped to this group's members. An earlier draft of
        // this used findAll() and filtered in memory, which quietly
        // loaded every account in the database on every group read.
        Map<String, UserSignup> byPhone = phones.isEmpty()
                ? Map.of()
                : userRepository.findByPhoneNumberIn(phones).stream()
                        .collect(Collectors.toMap(
                                UserSignup::getPhoneNumber,
                                user -> user,
                                (first, second) -> first));

        List<GroupMemberResponse> memberResponses = members.stream()
                .map(member -> {
                    UserSignup user = byPhone.get(member.getMemberPhone());

                    return GroupMemberResponse.builder()
                            .phone(member.getMemberPhone())
                            .name(user == null || user.getName() == null
                                    ? member.getMemberPhone()
                                    : user.getName())
                            .avatarId(user == null ? null : user.getAvatarId())
                            .role(member.getRole().name())
                            .build();
                })
                .toList();

        String myRole = members.stream()
                .filter(m -> m.getMemberPhone().equals(actorPhone))
                .map(m -> m.getRole().name())
                .findFirst()
                .orElse(null);

        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .name(group.getName())
                .avatarId(group.getAvatarId())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt() == null ? null : group.getCreatedAt().format(CREATED_FORMAT))
                .myRole(myRole)
                .members(memberResponses)
                .build();
    }
}
