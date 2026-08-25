package com.example.chatapp.controller.auth;

import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.entity.auth.request.RequestVerify;
import com.example.chatapp.entity.auth.responce.ResponseVerify;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// No @CrossOrigin here: Spring Security's CORS filter (see
// SecurityConfig.corsConfigurationSource) is authoritative for every
// request and runs ahead of this annotation, so it would be a no-op.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthUser {

    private final UserRepositorySignup userRepositorySignup;

    @PostMapping("/verify-user")
    public ResponseEntity<?> verifyUserPhoneNo(@RequestBody RequestVerify requestVerify) {
        UserSignup user = userRepositorySignup.findByPhoneNumber(requestVerify.getPhoneNumber())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ApiException("This user's email or phone number is not verified", HttpStatus.NOT_FOUND);
        }

        ResponseVerify responseVerify = new ResponseVerify();
        responseVerify.setName(user.getName());
        responseVerify.setEmail(user.getEmail());
        responseVerify.setPhoneNumber(user.getPhoneNumber());
        responseVerify.setPublicUserId(user.getPublicUserId());

        return ResponseEntity.ok(responseVerify);
    }
}
