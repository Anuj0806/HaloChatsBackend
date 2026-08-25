package com.example.chatapp.service.loginService.impl;

import com.example.chatapp.DTO.login.LoginRequest;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.service.loginService.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepositorySignup userRepositorySignup;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSignup login(LoginRequest request) {
        UserSignup user = userRepositorySignup
                .findByEmailOrPhoneNumber(request.getUsername())
                .orElseThrow(() -> new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ApiException("Please verify your email before logging in", HttpStatus.FORBIDDEN);
        }

        return user;
    }

    @Override
    public List<UserSignup> getAllUser() {

        return userRepositorySignup.findAll();
    }


}
