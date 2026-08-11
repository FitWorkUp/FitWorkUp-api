package com.fitworkup.repository;

import com.fitworkup.enums.StoreEffectType;
import com.fitworkup.models.UserBoost;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBoostRepository extends JpaRepository<UserBoost, Long> {
    Optional<UserBoost> findByUserIdAndEffectType(Long userId, StoreEffectType effectType);
    Optional<UserBoost> findByUserIdAndEffectTypeAndExpiresAtAfter(
            Long userId,
            StoreEffectType effectType,
            Instant now
    );
    List<UserBoost> findByUserIdAndExpiresAtAfterOrderByExpiresAtAsc(Long userId, Instant now);
}
