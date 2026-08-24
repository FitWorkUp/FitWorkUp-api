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

    List<Activity> findByUserIdAndIsValidTrueOrderByTimestampAsc(Long userId);

    @Query("SELECT a FROM Activity a JOIN FETCH a.user " +
           "WHERE a.isValid = true AND a.timestamp >= :start AND a.timestamp < :end")
    List<Activity> findValidActivitiesBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(a.distanceKm), 0.0) FROM Activity a WHERE a.user.id = :userId AND a.isValid = true")
    Double sumValidDistanceByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(COALESCE(a.acceptedSteps, a.steps, 0)), 0) FROM Activity a " +
           "WHERE a.user.id = :userId AND a.isValid = true AND a.timestamp >= :startOfDay")
    Long sumTodayValidSteps(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay
    );

    @Modifying
    @Query("UPDATE Activity a SET a.verificationMethod = 'ANONYMIZED', a.riskScore = 0 WHERE a.user.id = :userId")
    void anonymizeGpsDataByUserId(@Param("userId") Long userId);
}
