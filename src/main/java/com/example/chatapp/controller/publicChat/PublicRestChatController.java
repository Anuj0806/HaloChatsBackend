package com.example.chatapp.controller.publicChat;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.config.onlineTrackerPublic.OnlineUserTrackerPublic;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// No @CrossOrigin here: Spring Security's CORS filter (see
// SecurityConfig.corsConfigurationSource) is authoritative for every
// request and runs ahead of this annotation, so it would be a no-op.
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicRestChatController {

    // Looks up accounts in the "signup" table (UserRepositorySignup) - the
    // one accounts are actually created in via the OTP signup flow. The
    // legacy UserRepository/`users` table is no longer populated by any
    // active signup path, so looking users up there would always 404.
    private final UserRepositorySignup userRepositorySignup;

    /**
     * Unauthenticated, dependency-free reachability check.
     *
     * The frontend pings this on load and shows a plain-language
     * banner if it fails. Without it, a wrong VITE_API_HOST or a CORS
     * mismatch and a genuinely missing feature look identical to
     * someone testing the app: an empty contacts list, a "Message"
     * button that does nothing. This turns that ambiguity into an
     * explicit, actionable message the moment the page loads instead
     * of a silent failure discovered by clicking around.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("Halo backend is reachable", "ok"));
    }

    @GetMapping("/isOnline/{phone}")
    public ResponseEntity<Boolean> isOnline(@PathVariable String phone) {
        return ResponseEntity.ok(OnlineUserTrackerPublic.isOnlineInPublic(phone));
    }

    @GetMapping("/get/{phone}")
    public ResponseEntity<?> getUser(@PathVariable String phone) {
        return userRepositorySignup.getUserDTOByPhone(phone)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("User not found")));
    }
}
