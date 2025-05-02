package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // Lấy danh sách người đã trò chuyện với userId
    @Query("SELECT DISTINCT CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
            "FROM MessageEntity m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findDistinctParticipantIdsByUserId(@Param("userId") Long userId);

    // Lấy tin nhắn cuối cùng giữa userId và participantId
    @Query("SELECT m FROM MessageEntity m " +
            "WHERE (m.sender.id = :userId AND m.receiver.id = :participantId) " +
            "OR (m.sender.id = :participantId AND m.receiver.id = :userId) " +
            "ORDER BY m.timestamp DESC")
    List<MessageEntity> findLastMessageBetweenUsers(@Param("userId") Long userId,
                                                    @Param("participantId") Long participantId);

    // Đếm số tin nhắn chưa đọc từ participantId gửi đến userId
    @Query("SELECT COUNT(m) FROM MessageEntity m " +
            "WHERE m.sender.id = :participantId AND m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId, @Param("participantId") Long participantId);

    @Modifying
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markMessagesAsRead(Long senderId, Long receiverId);
}