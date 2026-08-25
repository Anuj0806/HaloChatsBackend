package com.example.chatapp.service.userService;

import com.example.chatapp.DTO.signUp.UserSignupRequest;
import com.example.chatapp.DTO.signUp.ResendOtpRequest;
import com.example.chatapp.DTO.signUp.VerifyOtp;

public interface UserServiceSignup {
    void registerUser(UserSignupRequest request);

    void verifyEmailOtp(VerifyOtp request);

    void resendEmailOtp(ResendOtpRequest request);
}
