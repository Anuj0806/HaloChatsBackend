package com.example.chatapp.controller.login;

import com.example.chatapp.DTO.login.LoginRequest;
import com.example.chatapp.DTO.login.LoginResponse;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.security.JwtUtil;
import com.example.chatapp.service.loginService.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class LoginController {

    private final LoginService loginService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login-user")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UserSignup user = loginService.login(request);
        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(
                new LoginResponse(
                        true,
                        "Welcome back",
                        user.getPublicUserId(),
                        user.getName(),
                        user.getPhoneNumber(),
                        token
                )
        );
    }


    @GetMapping("/users")
    public ResponseEntity<List<LoginRequest>> getAllUser() {
        List<UserSignup> users = loginService.getAllUser();
        List<LoginRequest> loginRequests = new ArrayList<>();
        for (UserSignup user : users) {
            LoginRequest add=new LoginRequest(user.getEmail(),"123456789");
            loginRequests.add(add);
        }

        return ResponseEntity.ok(loginRequests);
    }
}
