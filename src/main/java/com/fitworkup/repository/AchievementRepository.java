package com.fitworkup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitworkup.models.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    // CRUD básico já resolve para listar todas as conquistas disponíveis na loja/perfil
}