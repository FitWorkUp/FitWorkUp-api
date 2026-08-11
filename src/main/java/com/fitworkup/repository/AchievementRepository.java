package com.fitworkup.repository;

import com.fitworkup.models.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByName(String name);
    boolean existsByName(String name);
    List<Achievement> findByCriteriaTypeOrderByTargetValueAsc(String criteriaType);
}
