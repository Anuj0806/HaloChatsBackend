package com.example.chatapp.repo.privateRoom;

import com.example.chatapp.entity.chat.PrivateRoomPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivateRoomPinRepository extends JpaRepository<PrivateRoomPin, Long> {

    Optional<PrivateRoomPin> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
