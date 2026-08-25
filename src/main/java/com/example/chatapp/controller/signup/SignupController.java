package com.example.chatapp.controller.signup;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.signUp.ResendOtpRequest;
import com.example.chatapp.DTO.signUp.UserSignupRequest;
import com.example.chatapp.DTO.signUp.VerifyOtp;
import com.example.chatapp.service.userService.UserServiceSignup;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SignupController {

    private final UserServiceSignup userServiceSignup;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequest request) {
        userServiceSignup.registerUser(request);
        return ResponseEntity.ok(ApiResponse.ok("Your password has been sent to your email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtp request) {
        userServiceSignup.verifyEmailOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully"));
    }

    @PostMapping("/sendEmailOTP")
    public ResponseEntity<?> sendEmailOTP(@RequestBody ResendOtpRequest request) {
        userServiceSignup.resendEmailOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Email OTP sent successfully"));
    }
}
