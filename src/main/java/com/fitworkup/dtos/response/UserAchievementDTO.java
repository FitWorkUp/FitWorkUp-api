package com.fitworkup.dtos.response;

import java.time.LocalDateTime;

public record UserAchievementDTO(
    Long achievementId,
    String name,
    String description,
    String iconName,
    Integer xpReward,
    Integer fitCoinsReward,
    LocalDateTime unlockedAt
) {}