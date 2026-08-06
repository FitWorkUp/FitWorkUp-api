package com.fitworkup.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record UserAchievementDTO(
    Long achievementId,
    String name,
    String description,
    String iconName,
    Integer xpReward,
    @JsonProperty("fitcoinsReward")
    Integer fitCoinsReward,
    LocalDateTime unlockedAt
) {}