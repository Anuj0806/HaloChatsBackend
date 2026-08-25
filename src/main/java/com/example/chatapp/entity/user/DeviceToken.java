package com.example.chatapp.entity.user;

import com.example.chatapp.entity.UserSignup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "device_token")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_signup_id")
    private UserSignup userSignup;

    @Column(name = "device_id", unique = true)
    private String deviceId;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    private String platform;
}
