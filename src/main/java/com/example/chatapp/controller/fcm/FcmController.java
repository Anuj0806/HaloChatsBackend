package com.example.chatapp.controller.fcm;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.fcm.FcmTokenDTO;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.entity.user.DeviceToken;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.fcmtoken.FCMTokenRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class FcmController {

    private final UserRepositorySignup userRepositorySignup;
    private final FCMTokenRepository fcmTokenRepository;

    @PostMapping("/user/fcm-token")
    public ResponseEntity<?> updateFcmToken(@RequestBody FcmTokenDTO dto) {

        UserSignup user = userRepositorySignup.findByPublicUserId(dto.getPublicUserId())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Optional<DeviceToken> existingTokenOpt = fcmTokenRepository.findByDeviceId(dto.getDeviceId());

        DeviceToken token = existingTokenOpt.orElseGet(DeviceToken::new);
        token.setUserSignup(user);
        token.setDeviceId(dto.getDeviceId());
        token.setFcmToken(dto.getToken());
        token.setPlatform(dto.getPlatform());

        fcmTokenRepository.save(token);

        return ResponseEntity.ok(ApiResponse.ok("FCM token saved/updated successfully"));
    }
}
