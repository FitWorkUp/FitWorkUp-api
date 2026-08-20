package com.fitworkup.repository;

import com.fitworkup.models.GroupSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupSessionRepository extends JpaRepository<GroupSession, Long> {
    Optional<GroupSession> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
