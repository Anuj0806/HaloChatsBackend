package com.example.chatapp.service.userService.impl;

import com.example.chatapp.DTO.signUp.UserSignupRequest;
import com.example.chatapp.DTO.signUp.ResendOtpRequest;
import com.example.chatapp.DTO.signUp.VerifyOtp;
import com.example.chatapp.entity.EmailOtp;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.signup.EmailOtpRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.service.emailService.EmailService;
import com.example.chatapp.service.userService.UserServiceSignup;
import com.example.chatapp.utility.AllMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserServiceSignupImpl implements UserServiceSignup {

    private final UserRepositorySignup userRepositorySignup;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AllMethod allMethod;
    private final EmailService emailService;

    @Override
    @Transactional
    public void registerUser(UserSignupRequest request) {

        if (userRepositorySignup.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already registered");
        }

        if (userRepositorySignup.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new ApiException("Phone number already registered");
        }

        UserSignup user = new UserSignup();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCity(request.getCity());

//        String rawPassword = allMethod.generatePassword(9);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userRepositorySignup.save(user);

//        emailService.sendAccountEmail(
//                user.getEmail(),
//                "Account Created Successfully",
//                user.getName(),
//                user.getEmail(),
//                rawPassword
//        );

        // Registration leaves the account unverified - immediately issue an
        // OTP so the frontend can go straight from the signup form to a
        // "verify your email" screen without a separate button press.
        sendOtp(user);
    }

    private void sendOtp(UserSignup user) {
        emailOtpRepository.invalidateOldOtps(user.getEmail());

        String otp = allMethod.generateOtp();

        EmailOtp emailOtp = EmailOtp.builder()
                .userSignup(user)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        emailOtpRepository.save(emailOtp);

        emailService.sendOtpEmail(
                user.getEmail(),
                "Verify your account",
                user.getName(),
                otp
        );
    }

    @Override
    @Transactional
    public void verifyEmailOtp(VerifyOtp verifyOtp) {

        EmailOtp emailOtp = emailOtpRepository
                .findTopByUserSignup_EmailAndOtpCodeAndUsedFalseOrderByIdDesc(verifyOtp.getEmail(), verifyOtp.getOtp())
                .orElseThrow(() -> new ApiException("Invalid OTP"));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("OTP expired");
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        UserSignup user = emailOtp.getUserSignup();
        user.setIsVerified(true);
        userRepositorySignup.save(user);

//        emailService.sendAccountEmail(
//                user.getEmail(),
//                "Account Created Successfully",
//                user.getName(),
//                user.getEmail(),
//               "http://192.168.1.41:3000/updatePassword"
//        );

        emailOtpRepository.deleteAllByUserEmail(verifyOtp.getEmail());
    }
    @Override
    @Transactional
    public void resendEmailOtp(ResendOtpRequest request) {

        // Reused by both "resend my signup OTP" and the forgot-password
        // screen, so this deliberately does NOT gate on isVerified.
        UserSignup user = userRepositorySignup.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found"));

        sendOtp(user);
    }

}
