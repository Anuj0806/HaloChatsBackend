package com.example.chatapp.service.privateRoom.impl;

import com.example.chatapp.DTO.privateRoom.PinStatusResponse;
import com.example.chatapp.entity.UserSignup;
import com.example.chatapp.entity.chat.PrivateRoomPin;
import com.example.chatapp.exception.ApiException;
import com.example.chatapp.repo.privateRoom.PrivateRoomPinRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.service.privateRoom.PrivateRoomPinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateRoomPinServiceImpl implements PrivateRoomPinService {

    /*
    A 4-digit PIN has exactly 10,000 possible values. Without a limit
    on guessing, that is not a lock, it's a formality - a script can
    try all 10,000 in well under a minute against an endpoint with no
    throttle. Two things make it actually mean something:

      1. BCrypt hashing, so each guess costs the server real CPU time
         rather than a microsecond string comparison.
      2. A lockout that gets longer with each run of wrong guesses,
         capped at a sane maximum - not just "5 tries then locked
         forever," which would let anyone lock a person out of their
         own Private Room by deliberately failing 5 times.
    */
    private static final int MAX_ATTEMPTS_BEFORE_LOCK = 5;
    private static final Duration BASE_LOCKOUT = Duration.ofMinutes(1);
    private static final Duration MAX_LOCKOUT = Duration.ofMinutes(30);

    private final PrivateRoomPinRepository pinRepository;
    private final UserRepositorySignup userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PinStatusResponse status(String phone) {
        return pinRepository.findByPhone(phone)
                .map(pin -> {
                    long remaining = secondsRemaining(pin);
                    return PinStatusResponse.builder()
                            .hasPin(true)
                            .locked(remaining > 0)
                            .lockedForSeconds(remaining)
                            .build();
                })
                .orElseGet(() -> PinStatusResponse.builder()
                        .hasPin(false)
                        .locked(false)
                        .lockedForSeconds(0)
                        .build());
    }

    @Override
    @Transactional
    public void setPin(String phone, String currentPin, String newPin) {
        var existing = pinRepository.findByPhone(phone);

        if (existing.isPresent()) {
            PrivateRoomPin pin = existing.get();

            if (secondsRemaining(pin) > 0) {
                throw lockedException(pin);
            }

            if (currentPin == null || !passwordEncoder.matches(currentPin, pin.getPinHash())) {
                throw new ApiException("Your current PIN isn't right.", HttpStatus.UNAUTHORIZED);
            }

            pin.setPinHash(passwordEncoder.encode(newPin));
            pin.setFailedAttempts(0);
            pin.setLockedUntil(null);
            pinRepository.save(pin);
            return;
        }

        // First-time setup needs no current PIN - there isn't one yet.
        pinRepository.save(PrivateRoomPin.builder()
                .phone(phone)
                .pinHash(passwordEncoder.encode(newPin))
                .failedAttempts(0)
                .build());
    }

    @Override
    @Transactional
    public void verify(String phone, String submittedPin) {
        PrivateRoomPin pin = pinRepository.findByPhone(phone)
                .orElseThrow(() -> new ApiException(
                        "Set up a PIN for Private Room first.", HttpStatus.NOT_FOUND));

        long remaining = secondsRemaining(pin);
        if (remaining > 0) {
            throw lockedException(pin);
        }

        if (passwordEncoder.matches(submittedPin, pin.getPinHash())) {
            pin.setFailedAttempts(0);
            pin.setLockedUntil(null);
            pinRepository.save(pin);
            return;
        }

        int attempts = pin.getFailedAttempts() + 1;
        pin.setFailedAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS_BEFORE_LOCK) {
            // Doubles for every lockout beyond the first, capped so a
            // determined attacker can't be locked out for literally
            // forever by their own failed guesses - or, just as
            // importantly, so a third party can't weaponise the lockout
            // itself to deny the real owner access indefinitely.
            int lockoutsSoFar = attempts / MAX_ATTEMPTS_BEFORE_LOCK;
            long minutes = Math.min(
                    BASE_LOCKOUT.toMinutes() * (1L << Math.min(lockoutsSoFar - 1, 10)),
                    MAX_LOCKOUT.toMinutes()
            );
            pin.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));

            log.warn("Private Room PIN locked for {} after {} failed attempts", phone, attempts);
        }

        pinRepository.save(pin);

        if (pin.getLockedUntil() != null) {
            throw lockedException(pin);
        }

        int left = MAX_ATTEMPTS_BEFORE_LOCK - attempts;
        throw new ApiException(
                "Wrong PIN. " + left + " attempt" + (left == 1 ? "" : "s") + " left before this locks.",
                HttpStatus.UNAUTHORIZED);
    }

    @Override
    @Transactional
    public void resetWithAccountPassword(String phone, String accountPassword, String newPin) {
        UserSignup account = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new ApiException("Account not found", HttpStatus.NOT_FOUND));

        if (accountPassword == null || !passwordEncoder.matches(accountPassword, account.getPasswordHash())) {
            throw new ApiException("Your account password isn't right.", HttpStatus.UNAUTHORIZED);
        }

        PrivateRoomPin pin = pinRepository.findByPhone(phone)
                .orElseGet(() -> PrivateRoomPin.builder().phone(phone).build());

        pin.setPinHash(passwordEncoder.encode(newPin));
        pin.setFailedAttempts(0);
        pin.setLockedUntil(null);

        pinRepository.save(pin);

        log.info("Private Room PIN reset via account password for {}", phone);
    }

    private long secondsRemaining(PrivateRoomPin pin) {
        if (pin.getLockedUntil() == null) return 0;

        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), pin.getLockedUntil());
        return Math.max(0, seconds);
    }

    private ApiException lockedException(PrivateRoomPin pin) {
        long seconds = secondsRemaining(pin);
        long minutes = Math.max(1, (seconds + 59) / 60);

        return new ApiException(
                "Too many wrong PINs. Try again in " + minutes + " minute" + (minutes == 1 ? "" : "s") + ".",
                HttpStatus.TOO_MANY_REQUESTS);
    }
}
