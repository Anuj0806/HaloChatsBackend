package com.example.chatapp.security;

import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Turns an Authorization header into the account that sent it.
 *
 * This logic was copy-pasted into three controllers. That is exactly
 * the kind of duplication where one copy eventually forgets to call
 * validateToken() and quietly becomes an authentication bypass, so it
 * lives in one place now.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final JwtUtil jwtUtil;
    private final UserRepositorySignup userRepository;

    /** The caller's email, without touching the database. */
    public String emailOf(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ApiException("Sign in to continue", HttpStatus.UNAUTHORIZED);
        }

        String token = authorizationHeader.replace("Bearer ", "").trim();

        if (!jwtUtil.validateToken(token)) {
            throw new ApiException("Your session has expired. Sign in again.", HttpStatus.UNAUTHORIZED);
        }

        return jwtUtil.getEmailFromToken(token);
    }

    public UserSignup userOf(String authorizationHeader) {
        String email = emailOf(authorizationHeader);

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    /**
     * The caller's phone number, which is the identity everything in
     * the chat layer is keyed on.
     */
    public String phoneOf(String authorizationHeader) {
        return userOf(authorizationHeader).getPhoneNumber();
    }
}
