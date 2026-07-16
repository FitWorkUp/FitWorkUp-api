package com.fitworkup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitworkup.models.UserAchievement;

import java.util.List;


public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    // Busca todas as conquistas que um usuário específico já desbloqueou
    List<UserAchievement> findByUserId(Long userId);
    
    // Verifica se o usuário já desbloqueou uma conquista específica para não dar prêmio duplicado
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
}