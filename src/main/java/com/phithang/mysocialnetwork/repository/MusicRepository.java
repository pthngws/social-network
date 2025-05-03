package com.phithang.mysocialnetwork.repository;

import com.phithang.mysocialnetwork.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicRepository extends JpaRepository<Music, Long> {
}