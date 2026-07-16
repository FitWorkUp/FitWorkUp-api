package com.fitworkup.repository;

import com.fitworkup.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Busca o histórico de atividades de um usuário específico, ordenando pelas mais recentes
    List<Activity> findByUserIdOrderByTimestampDesc(Long userId);
}