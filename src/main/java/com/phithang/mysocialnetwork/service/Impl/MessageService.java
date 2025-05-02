package com.phithang.mysocialnetwork.service.Impl;

import com.phithang.mysocialnetwork.dto.request.MessageDto;
import com.phithang.mysocialnetwork.dto.response.ConversationDto;
import com.phithang.mysocialnetwork.entity.MessageEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import com.phithang.mysocialnetwork.exception.AppException;
import com.phithang.mysocialnetwork.exception.ErrorCode;
import com.phithang.mysocialnetwork.repository.MessageRepository;
import com.phithang.mysocialnetwork.service.IMessageService;
import com.phithang.mysocialnetwork.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private MessageRepository chatRepository;

    @Autowired
    private IUserService userService;

    @Autowired
    private OnlineStatusService onlineStatusService;

    @Autowired
    private NotificationService notificationService;



    @Override
    public List<ConversationDto> findDistinctParticipantsByUserId(Long receiverId) {
        UserEntity receiver = userService.findById(receiverId);
        if (receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }

        // Lấy danh sách ID người đã trò chuyện
        List<Long> participantIds = chatRepository.findDistinctParticipantIdsByUserId(receiverId);
        List<ConversationDto> conversations = new ArrayList<>();

        for (Long participantId : participantIds) {
            // Lấy thông tin người dùng
            UserEntity participant = userService.findById(participantId);
            if (participant == null) {
                continue; // Bỏ qua nếu người dùng không tồn tại
            }

            // Tạo DTO
            ConversationDto conversation = new ConversationDto();
            conversation.setUserID(participantId);
            conversation.setName(participant.getFirstname() + " " + participant.getLastname());
            conversation.setAvatar(participant.getImageUrl() != null ? participant.getImageUrl() : "");

            // Lấy tin nhắn cuối cùng
            List<MessageEntity> lastMessages = chatRepository.findLastMessageBetweenUsers(receiverId, participantId);
            if (!lastMessages.isEmpty()) {
                MessageEntity lastMessage = lastMessages.get(0); // Lấy tin nhắn đầu tiên (mới nhất)
                conversation.setContent(lastMessage.getContent());
                conversation.setLastMessageTime(lastMessage.getTimestamp());
                conversation.setIsSender(lastMessage.getSender().getId().equals(receiverId));
            }

            // Đếm số tin nhắn chưa đọc
            Long unreadCount = chatRepository.countUnreadMessages(receiverId, participantId);
            conversation.setUnreadCount(unreadCount);

            // Lấy trạng thái online
            boolean isOnline = onlineStatusService.isUserOnline(participantId);
            Long minutesAgo = onlineStatusService.getLastSeenMinutesAgo(participantId);
            conversation.setIsOnline(isOnline);
            conversation.setMinutesAgo(minutesAgo);

            conversations.add(conversation);
        }

        // Sắp xếp theo thời gian tin nhắn cuối cùng (mới nhất trước)
        conversations.sort((c1, c2) -> {
            if (c1.getLastMessageTime() == null) return 1;
            if (c2.getLastMessageTime() == null) return -1;
            return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
        });

        return conversations;
    }

    @Override
    public MessageEntity save(MessageDto chatEntity) {
        UserEntity sender = userService.findById(chatEntity.getSenderId());
        UserEntity receiver = userService.findById(chatEntity.getReceiverId());
        if (sender == null || receiver == null) {
            throw new AppException(ErrorCode.USER_NOT_EXIST);
        }
        chatEntity.setTimestamp(java.time.LocalDateTime.now());
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setContent(chatEntity.getContent());
        messageEntity.setReceiver(receiver);
        messageEntity.setSender(sender);
        messageEntity.setTimestamp(chatEntity.getTimestamp());
        try {
            return chatRepository.save(messageEntity);
        } catch (Exception e) {
            throw new AppException(ErrorCode.MESSAGE_CREATION_FAILED);
        }
    }

    @Override
    public List<Map<String, Object>> findMessagesBetweenUsers(Long senderId, Long receiverId) {
        return chatRepository.findAll().stream()
                .filter(row -> (row.getSender().getId().equals(senderId) && row.getReceiver().getId().equals(receiverId)) ||
                        (row.getSender().getId().equals(receiverId) && row.getReceiver().getId().equals(senderId)))
                .map(row -> {
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("id", row.getId());
                    messageMap.put("contentMessage", row.getContent());
                    messageMap.put("timestamp", row.getTimestamp());
                    messageMap.put("senderID", row.getSender().getId());
                    messageMap.put("receiverID", row.getReceiver().getId());
                    messageMap.put("isRead", row.isRead());
                    return messageMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void markMessagesAsRead(Long senderId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String email = authentication.getName();
        UserEntity userEntity = userService.findUserByEmail(email);
        Long receiverId = userEntity.getId();
        chatRepository.markMessagesAsRead(senderId, receiverId);
    }

}