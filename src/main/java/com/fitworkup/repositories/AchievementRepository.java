package com.fitworkup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitworkup.models.Achievement;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    // CRUD básico já resolve para listar todas as conquistas disponíveis na loja/perfil
}