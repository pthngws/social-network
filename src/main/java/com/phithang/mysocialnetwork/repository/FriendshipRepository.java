package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.FriendshipEntity;
import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {
    @Query("SELECT f FROM FriendshipEntity f WHERE (f.user1 = :sender AND f.user2 = :receiver) " +
            "OR (f.user1 = :receiver AND f.user2 = :sender)")
    FriendshipEntity findBySenderAndReceiver(@Param("sender") UserEntity sender, @Param("receiver") UserEntity receiver);

    @Query("SELECT f FROM FriendshipEntity f WHERE (f.user1 = :sender AND f.user2 = :receiver) OR (f.user1 = :receiver AND f.user2 = :sender)")
    FriendshipEntity findByUser1AndUser2(@Param("sender") UserEntity sender, @Param("receiver") UserEntity receiver);



    @Query("SELECT f FROM FriendshipEntity f WHERE f.user2 = :userEntity and f.status= 'PENDING' ")
    List<FriendshipEntity> findAllRequests(UserEntity userEntity);

    @Query("SELECT f FROM FriendshipEntity f WHERE (f.user2 = :userEntity OR f.user1 = :userEntity) AND f.status = 'ACCEPTED'")
    List<FriendshipEntity> findAllFriends(UserEntity userEntity);

}
