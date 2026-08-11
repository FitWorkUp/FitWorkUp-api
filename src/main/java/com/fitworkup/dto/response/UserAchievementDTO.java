package com.fitworkup.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record UserAchievementDTO(
        Long id,
        String name,
        String description,
        boolean unlocked,
        LocalDateTime unlockedAt,
        String iconName,
        Integer xpReward,
        @JsonProperty("fitcoinsReward") Integer fitCoinsReward
) {}
