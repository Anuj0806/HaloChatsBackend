package com.example.chatapp.service.chat.impl;

import com.example.chatapp.DTO.publicChat.*;
import com.example.chatapp.config.onlineTrackerPublic.OnlineUserTrackerPublic;
import com.example.chatapp.entity.chat.ChatMessageEntity;
import com.example.chatapp.entity.enumData.MessageStatus;
import com.example.chatapp.repo.chat.ChatMessageRepository;
import com.example.chatapp.repo.fcmtoken.FCMTokenRepository;
import com.example.chatapp.repo.signup.UserRepositorySignup;
import com.example.chatapp.service.chat.ChatService;
import com.example.chatapp.service.group.GroupService;
import com.example.chatapp.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final FCMTokenRepository fcmTokenRepository;
    private final FcmService fcmService;
    private final GroupService groupService;
    private final UserRepositorySignup userRepository;

    @Override
    public void sendMessage(PublicChatDTO chatMessage) {
        if (chatMessage.getGroupId() != null && !chatMessage.getGroupId().isBlank()) {
            sendGroupMessage(chatMessage);
        } else {
            sendDirectMessage(chatMessage);
        }
    }

    private void sendDirectMessage(PublicChatDTO chatMessage) {
        persist(chatMessage, chatMessage.getReceiver(), null);

        deliver(chatMessage.getReceiver(), chatMessage);
        ackSent(chatMessage);
    }

    /**
     * Fan-out. One inbound message becomes one stored copy and one
     * delivery per member, minus the sender.
     *
     * The sender's own copy is deliberately not stored or echoed: their
     * client already wrote the message locally before publishing, and
     * echoing it back would show it twice.
     */
    private void sendGroupMessage(PublicChatDTO chatMessage) {
        String groupId = chatMessage.getGroupId();
        String sender = chatMessage.getSender();

        // Without this check, anyone who learns a groupId could post
        // into a conversation they were never part of.
        if (!groupService.isMember(groupId, sender)) {
            log.warn("Rejected group message from non-member {} to {}", sender, groupId);
            return;
        }

        String senderName = chatMessage.getSenderName() != null && !chatMessage.getSenderName().isBlank()
                ? chatMessage.getSenderName()
                : userRepository.findByPhoneNumber(sender)
                        .map(user -> user.getName() == null ? sender : user.getName())
                        .orElse(sender);

        List<String> recipients = groupService.memberPhones(groupId).stream()
                .filter(phone -> !phone.equals(sender))
                .toList();

        if (recipients.isEmpty()) {
            log.debug("Group {} has no recipients besides the sender", groupId);
            ackSent(chatMessage);
            return;
        }

        for (String recipient : recipients) {
            persist(chatMessage, recipient, senderName);

            PublicChatDTO copy = new PublicChatDTO(
                    chatMessage.getMessageId(),
                    sender,
                    recipient,
                    chatMessage.getPayload(),
                    chatMessage.getTimeStamp(),
                    groupId,
                    senderName
            );

            deliver(recipient, copy);
        }

        ackSent(chatMessage);
    }

    private void persist(PublicChatDTO message, String receiver, String senderName) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setMessageId(message.getMessageId());
        entity.setSender(message.getSender());
        entity.setReceiver(receiver);
        entity.setGroupId(message.getGroupId());
        entity.setSenderName(senderName);
        entity.setPayload(message.getPayload());
        entity.setStatus(MessageStatus.SENT);
        entity.setSendAt(message.getTimeStamp());
        entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        chatMessageRepository.save(entity);
    }

    private void deliver(String recipient, PublicChatDTO payload) {
        if (recipient == null) {
            return;
        }

        if (OnlineUserTrackerPublic.isOnlineInPublic(recipient)) {
            messagingTemplate.convertAndSendToUser(recipient, "/queue/receiveMessage", payload);
            log.debug("Delivered instantly to online user {}", recipient);
            return;
        }

        // Offline: the copy stays in chat_messages and is replayed on
        // their next /chat.ready. The push is only a nudge.
        fcmTokenRepository.getFcmToken(recipient).ifPresentOrElse(
                fcmService::sendFcmNotification,
                () -> log.debug("Receiver {} offline and has no FCM token registered", recipient)
        );
    }

    /**
     * One ack per message, not per recipient. The sender's UI shows a
     * single tick for the message as a whole; per-member state would
     * need a different wire shape entirely.
     */
    private void ackSent(PublicChatDTO chatMessage) {
        AckDTO ack = new AckDTO();
        ack.setMessageId(chatMessage.getMessageId());
        ack.setStatus(AckDTO.SENT);

        messagingTemplate.convertAndSendToUser(chatMessage.getSender(), "/queue/ack", ack);
    }

    @Override
    public void deliverPendingMessages(String username) {
        List<ChatMessageEntity> pending =
                chatMessageRepository.findByReceiverAndStatus(username, MessageStatus.SENT);

        for (ChatMessageEntity msg : pending) {
            // groupId and senderName have to survive the replay, or a
            // group message that arrived while offline would reappear
            // as a one-to-one message from a stranger's number.
            PublicChatDTO dto = new PublicChatDTO(
                    msg.getMessageId(),
                    msg.getSender(),
                    msg.getReceiver(),
                    msg.getPayload(),
                    msg.getSendAt(),
                    msg.getGroupId(),
                    msg.getSenderName()
            );

            messagingTemplate.convertAndSendToUser(username, "/queue/receiveMessage", dto);
        }
    }

    @Override
    @Transactional
    public void acknowledgeDelivered(String messageId, String receiver) {
        if (receiver == null || receiver.isBlank()) {
            // No authenticated principal: fall back to the old
            // behaviour rather than leaving the row forever.
            chatMessageRepository.deleteByMessageId(messageId);
            return;
        }

        // Scoped to this recipient. Deleting by messageId alone would
        // discard every other group member's undelivered copy as soon
        // as the first device confirmed receipt.
        chatMessageRepository.deleteByMessageIdAndReceiver(messageId, receiver);
    }

    @Override
    @Transactional
    public void markAsRead(ReadReceiptDTO receipt) {
        // Scoped to the reader: in a group, five people each have their
        // own copy, and marking "the" copy read would be arbitrary.
        chatMessageRepository
                .findByMessageIdAndReceiver(receipt.getMessageId(), receipt.getReader())
                .ifPresent(entity -> {
                    entity.setStatus(MessageStatus.READ);
                    chatMessageRepository.save(entity);
                });

        if (receipt.getSender() != null) {
            AckDTO ack = new AckDTO();
            ack.setMessageId(receipt.getMessageId());
            ack.setStatus(AckDTO.READ);
            ack.setInfo(receipt.getReader());
            messagingTemplate.convertAndSendToUser(receipt.getSender(), "/queue/ack", ack);
        }
    }

    @Override
    @Transactional
    public void addReaction(ReactionDTO reaction) {
        chatMessageRepository.findByMessageId(reaction.getMessageId()).ifPresent(entity -> {
            String existing = entity.getReactions();
            String addition = reaction.getSender() + ":" + reaction.getEmoji();
            entity.setReactions(existing == null || existing.isBlank() ? addition : existing + "," + addition);
            chatMessageRepository.save(entity);
        });

        if (reaction.getReceiver() != null) {
            messagingTemplate.convertAndSendToUser(reaction.getReceiver(), "/queue/reaction", reaction);
        }
    }

    @Override
    public void notifyTyping(TypingDTO typing) {
        String groupId = typing.getGroupId();

        if (groupId != null && !groupId.isBlank()) {
            if (!groupService.isMember(groupId, typing.getSender())) {
                return;
            }

            groupService.memberPhones(groupId).stream()
                    .filter(phone -> !phone.equals(typing.getSender()))
                    .forEach(phone -> messagingTemplate.convertAndSendToUser(
                            phone, "/queue/typing", typing));
            return;
        }

        if (typing.getReceiver() != null) {
            messagingTemplate.convertAndSendToUser(typing.getReceiver(), "/queue/typing", typing);
        }
    }
}
