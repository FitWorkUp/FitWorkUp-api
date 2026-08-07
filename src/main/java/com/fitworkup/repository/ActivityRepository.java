package com.fitworkup.repository;

import com.fitworkup.models.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId AND a.isValid = true AND a.timestamp >= :startOfDay")
    List<Activity> findTodayValidActivities(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    List<Activity> findByUserIdOrderByTimestampDesc(Long userId);

    List<Activity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Activity a SET a.verificationMethod = 'ANONYMIZED', a.riskScore = 0 WHERE a.user.id = :userId")
    void anonymizeGpsDataByUserId(@Param("userId") Long userId);
}