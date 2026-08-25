package com.example.chatapp.repo.fcmtoken;

import com.example.chatapp.entity.user.DeviceToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FCMTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByDeviceId(String deviceId);

    @Query("SELECT d.fcmToken FROM DeviceToken d WHERE d.userSignup.publicUserId = :publicUserId")
    Optional<String> getFcmToken(@Param("publicUserId") String publicUserId);
}
