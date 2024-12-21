package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface MediaRepository extends JpaRepository<MediaEntity, Long> {


}
