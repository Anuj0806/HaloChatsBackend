package com.example.chatapp.controller.auth;

import com.example.chatapp.DTO.auth.ChangePasswordRequest;
import com.example.chatapp.DTO.auth.ForgotPasswordRequest;
import com.example.chatapp.DTO.auth.ResetPasswordRequest;
import com.example.chatapp.DTO.auth.ResetTokenResponse;
import com.example.chatapp.DTO.auth.VerifyResetOtpRequest;
import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.security.CurrentUserResolver;
import com.example.chatapp.service.passwordReset.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final CurrentUserResolver currentUser;

    /** Step 1 - email a one-time code. */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestReset(request);

        // Same response whether or not the address is registered, so this
        // can't be used to check who has an account here.
        return ResponseEntity.ok(ApiResponse.ok(
                "If that address has an account, a 6-digit code is on its way."));
    }

    /** Step 2 - trade a valid code for a short-lived reset token. */
    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ApiResponse<ResetTokenResponse>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {

        ResetTokenResponse token = passwordResetService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Code accepted", token));
    }

    /** Step 3 - trade the token plus a new password for a new hash. */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated. Sign in with your new password."));
    }

    /** Signed-in equivalent, for the Settings screen. */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {

        String email = currentUser.emailOf(authorization);
        passwordResetService.changePassword(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated"));
    }

}
