package com.example.chatapp.controller.privateRoom;

import com.example.chatapp.DTO.common.ApiResponse;
import com.example.chatapp.DTO.privateRoom.PinStatusResponse;
import com.example.chatapp.DTO.privateRoom.ResetPinRequest;
import com.example.chatapp.DTO.privateRoom.SetPinRequest;
import com.example.chatapp.DTO.privateRoom.VerifyPinRequest;
import com.example.chatapp.security.CurrentUserResolver;
import com.example.chatapp.service.privateRoom.PrivateRoomPinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/private-room/pin")
public class PrivateRoomPinController {

    private final PrivateRoomPinService pinService;
    private final CurrentUserResolver currentUser;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PinStatusResponse>> status(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String phone = currentUser.phoneOf(authorization);
        return ResponseEntity.ok(ApiResponse.ok("Status loaded", pinService.status(phone)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> setPin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SetPinRequest request) {

        String phone = currentUser.phoneOf(authorization);
        pinService.setPin(phone, request.getCurrentPin(), request.getNewPin());

        return ResponseEntity.ok(ApiResponse.ok("PIN saved"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody VerifyPinRequest request) {

        String phone = currentUser.phoneOf(authorization);
        pinService.verify(phone, request.getPin());

        return ResponseEntity.ok(ApiResponse.ok("Unlocked"));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> reset(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ResetPinRequest request) {

        String phone = currentUser.phoneOf(authorization);
        pinService.resetWithAccountPassword(phone, request.getAccountPassword(), request.getNewPin());

        return ResponseEntity.ok(ApiResponse.ok("PIN reset"));
    }
}
