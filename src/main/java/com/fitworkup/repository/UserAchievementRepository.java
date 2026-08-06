package com.fitworkup.repository;

import com.fitworkup.models.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    // JOIN FETCH carrega as informações do Achievement de forma eager em 1 consulta
    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId")
    List<UserAchievement> findByUserIdWithDetails(@Param("userId") Long userId);

    List<UserAchievement> findByUserId(Long userId);

    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
}