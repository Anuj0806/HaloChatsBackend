package com.example.chatapp.controller.privateChat;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.config.onlineTrackerPrivate.OnlineUserTracker;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// No @CrossOrigin here: Spring Security's CORS filter (see
// SecurityConfig.corsConfigurationSource) is authoritative for every
// request and runs ahead of this annotation, so it would be a no-op.
@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class PrivateUserController {

    // Looks up accounts in the "signup" table (UserRepositorySignup) - the
    // one accounts are actually created in via the OTP signup flow. The
    // legacy UserRepository/`users` table is no longer populated by any
    // active signup path, so looking users up there would always 404.
    private final UserRepositorySignup userRepositorySignup;

    @GetMapping("/isOnline/{phone}")
    public ResponseEntity<Boolean> isOnline(@PathVariable String phone) {
        return ResponseEntity.ok(OnlineUserTracker.isOnline(phone));
    }

    @GetMapping("/get/{phone}")
    public ResponseEntity<?> getUser(@PathVariable String phone) {
        return userRepositorySignup.getUserDTOByPhone(phone)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("User not found")));
    }
}
