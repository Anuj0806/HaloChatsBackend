package com.example.chatapp.controller.profile;

import com.example.chatapp.DTO.UserDTO;
import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.profile.ProfileResponse;
import com.example.chatapp.DTO.profile.UpdateProfileRequest;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.security.CurrentUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private static final DateTimeFormatter JOINED_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final UserRepositorySignup userRepository;
    private final CurrentUserResolver currentUser;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> me(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        UserSignup user = currentUser.userOf(authorization);
        return ResponseEntity.ok(ApiResponse.ok("Profile loaded", toResponse(user)));
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<ApiResponse<ProfileResponse>> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserSignup user = currentUser.userOf(authorization);

        user.setName(request.getName().trim());

        // Blank and null both mean "clear it", so a user can remove their
        // status line rather than being stuck with whatever they first set.
        user.setAbout(isBlank(request.getAbout()) ? null : request.getAbout().trim());

        if (!isBlank(request.getAvatarId())) {
            user.setAvatarId(request.getAvatarId().trim());
        }

        if (!isBlank(request.getCity())) {
            user.setCity(request.getCity().trim());
        }

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Profile saved", toResponse(user)));
    }

    /**
     * Finds people to start a chat with. Matches on name or phone so a user
     * doesn't have to know someone's exact number to reach them.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDTO>>> search(
            @RequestParam("q") String term,
            @RequestParam(value = "self", required = false, defaultValue = "") String selfPhone) {

        String trimmed = term == null ? "" : term.trim();

        if (trimmed.length() < 2) {
            return ResponseEntity.ok(ApiResponse.ok("Type at least 2 characters", List.of()));
        }

        List<UserDTO> matches = userRepository.searchDirectory(trimmed, selfPhone);
        String message = matches.isEmpty() ? "No one matched that" : "Found " + matches.size();

        return ResponseEntity.ok(ApiResponse.ok(message, matches));
    }

    private ProfileResponse toResponse(UserSignup user) {
        return ProfileResponse.builder()
                .publicUserId(user.getPublicUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .city(user.getCity())
                .about(user.getAbout())
                .avatarId(user.getAvatarId())
                .verified(Boolean.TRUE.equals(user.getIsVerified()))
                .joinedAt(user.getCreatedAt() == null ? null : user.getCreatedAt().format(JOINED_FORMAT))
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
