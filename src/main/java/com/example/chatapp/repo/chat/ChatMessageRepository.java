package com.example.chatapp.repo.chat;

import com.example.chatapp.entity.chat.ChatMessageEntity;
import com.example.chatapp.entity.enumData.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByReceiverAndStatus(String receiver, MessageStatus status);

    Optional<ChatMessageEntity> findByMessageId(String messageId);

    Optional<ChatMessageEntity> findByMessageIdAndReceiver(String messageId, String receiver);

    /**
     * Deletes only the acknowledging recipient's copy. A group message
     * exists once per member, so deleting by messageId alone would drop
     * everyone else's undelivered copy the moment the first device
     * confirmed receipt.
     */
    void deleteByMessageIdAndReceiver(String messageId, String receiver);

    void deleteByMessageId(String messageId);
}
