package com.phithang.mysocialnetwork.repository;


import com.phithang.mysocialnetwork.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    UserEntity findByEmailAndPassword(String email, String password);

    @Query("SELECT u FROM UserEntity u WHERE u.firstname LIKE %:name% OR u.lastname LIKE %:name%")
    List<UserEntity> findByFirstnameOrLastnameContaining(@Param("name") String name);

}
