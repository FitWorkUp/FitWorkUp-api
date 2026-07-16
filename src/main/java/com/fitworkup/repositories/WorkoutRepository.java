package com.fitworkup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitworkup.models.Workout;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    
    // Busca o histórico de treinos de um usuário específico ordenando pela data mais recente
    List<Workout> findByUserIdOrderByDateTimeDesc(Long userId);
}