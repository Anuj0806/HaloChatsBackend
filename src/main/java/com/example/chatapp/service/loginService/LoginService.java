package com.example.chatapp.service.loginService;

import com.example.chatapp.DTO.login.LoginRequest;
import com.example.chatapp.entity.UserSignup;

import java.util.List;

public interface LoginService {
    UserSignup login(LoginRequest request);

    List<UserSignup> getAllUser();
}
