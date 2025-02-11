package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    @Query("SELECT DISTINCT " +
            "COALESCE(u.id, c.sender.id) as userID, " +
            "COALESCE(CONCAT(u.firstname, ' ', u.lastname), CAST(c.sender.id AS string)) as name " +
            "FROM MessageEntity c " +
            "LEFT JOIN UserEntity u ON c.sender.id = u.id OR c.receiver.id = u.id " +
            "WHERE (c.sender.id = :userId OR c.receiver.id = :userId) " +
            "AND (u.id != :userId OR u.id IS NULL)")
    List<Map<String, Object>> findDistinctParticipantsByUserId(@Param("userId") Long userId);





}

