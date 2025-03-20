package com.phithang.mysocialnetwork.repository;


import com.phithang.mysocialnetwork.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity,Long> {
}
