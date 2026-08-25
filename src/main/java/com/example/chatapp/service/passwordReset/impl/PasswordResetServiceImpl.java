package com.example.chatapp.service.passwordReset.impl;

import com.example.chatapp.DTO.auth.ChangePasswordRequest;
import com.example.chatapp.DTO.auth.ForgotPasswordRequest;
import com.example.chatapp.DTO.auth.ResetPasswordRequest;
import com.example.chatapp.DTO.auth.ResetTokenResponse;
import com.example.chatapp.DTO.auth.VerifyResetOtpRequest;
import com.example.chatapp.entity.EmailOtp;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.signup.EmailOtpRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.security.ResetTokenService;
import com.example.chatapp.service.emailService.EmailService;
import com.example.chatapp.service.passwordReset.PasswordResetService;
import com.example.chatapp.utility.AllMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    /** Minimum gap between reset emails for the same address. */
    private static final long RESEND_COOLDOWN_MS = 60_000;

    private static final int OTP_VALIDITY_MINUTES = 10;

    private final UserRepositorySignup userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetTokenService resetTokenService;
    private final EmailService emailService;
    private final AllMethod allMethod;

    /**
     * In-memory throttle. Enough to stop someone hammering "resend" and
     * turning this endpoint into a way to spam a stranger's inbox. A
     * multi-instance deployment would move this to Redis.
     */
    private final Map<String, Long> lastSentAt = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = normalise(request.getEmail());

        // Deliberately does NOT tell the caller whether the address exists.
        // The controller always responds "if that address has an account,
        // we've sent a code", so this endpoint can't be used to discover
        // who is registered.
        Optional<UserSignup> found = userRepository.findByEmailIgnoreCase(email);
        if (found.isEmpty()) {
            log.info("Password reset requested for unknown address");
            return;
        }

        UserSignup user = found.get();

        Long previous = lastSentAt.get(email);
        if (previous != null && System.currentTimeMillis() - previous < RESEND_COOLDOWN_MS) {
            throw new ApiException(
                    "A code was just sent. Wait a minute before asking for another.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        emailOtpRepository.invalidateOldOtps(user.getEmail());

        String otp = allMethod.generateOtp();

        emailOtpRepository.save(EmailOtp.builder()
                .userSignup(user)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .used(false)
                .build());

        emailService.sendOtpEmail(
                user.getEmail(),
                "Reset your password",
                user.getName(),
                otp);

        lastSentAt.put(email, System.currentTimeMillis());
    }

    @Override
    @Transactional
    public ResetTokenResponse verifyResetOtp(VerifyResetOtpRequest request) {
        String email = normalise(request.getEmail());

        UserSignup user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(
                        "That code doesn't match. Check the email and try again.",
                        HttpStatus.BAD_REQUEST));

        EmailOtp emailOtp = emailOtpRepository
                .findTopByUserSignup_EmailAndOtpCodeAndUsedFalseOrderByIdDesc(user.getEmail(), request.getOtp().trim())
                .orElseThrow(() -> new ApiException(
                        "That code doesn't match. Check the email and try again.",
                        HttpStatus.BAD_REQUEST));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    "That code has expired. Request a new one.",
                    HttpStatus.BAD_REQUEST);
        }

        // Burn the code immediately - it has done its job, and leaving it
        // usable would let the same code mint more than one reset token.
        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        String token = resetTokenService.issue(user.getEmail(), user.getPasswordHash());

        return new ResetTokenResponse(token, ResetTokenService.VALIDITY_SECONDS);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalise(request.getEmail());

        UserSignup user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(
                        "This reset link has expired. Request a new code to continue.",
                        HttpStatus.BAD_REQUEST));

        // Throws if the token is forged, expired, for a different account,
        // or already spent (the hash fingerprint won't match any more).
        resetTokenService.validate(request.getResetToken(), user.getEmail(), user.getPasswordHash());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ApiException("Choose a password you haven't used before.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // Someone who can reset a password has proven they own the mailbox,
        // which is exactly what email verification checks. An account stuck
        // unverified can recover this way rather than being locked out.
        user.setIsVerified(true);

        userRepository.save(user);

        emailOtpRepository.deleteAllByUserEmail(user.getEmail());
        lastSentAt.remove(email);

        log.info("Password reset completed for user id {}", user.getId());
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        UserSignup user = userRepository.findByEmailIgnoreCase(normalise(email))
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException("Your current password isn't right.", HttpStatus.UNAUTHORIZED);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ApiException("Choose a password you haven't used before.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String normalise(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
