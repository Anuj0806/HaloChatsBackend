package com.example.chatapp.service.passwordReset;

import com.example.chatapp.DTO.auth.ChangePasswordRequest;
import com.example.chatapp.DTO.auth.ForgotPasswordRequest;
import com.example.chatapp.DTO.auth.ResetPasswordRequest;
import com.example.chatapp.DTO.auth.ResetTokenResponse;
import com.example.chatapp.DTO.auth.VerifyResetOtpRequest;

/**
 * The three-step forgot-password flow, plus the signed-in equivalent.
 *
 *   1. requestReset      - email a one-time code
 *   2. verifyResetOtp    - exchange a valid code for a short-lived reset token
 *   3. resetPassword     - exchange the token + a new password for a new hash
 */
public interface PasswordResetService {

    void requestReset(ForgotPasswordRequest request);

    ResetTokenResponse verifyResetOtp(VerifyResetOtpRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String email, ChangePasswordRequest request);
}
