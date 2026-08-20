package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserProfileDTO {
    private Long id;
    private String username;
    private Integer xp;
    private Integer nextLevelXp;
    private Integer level;
    private Integer streak;
    private Double totalDistanceKm;
    private String avatarBorder;
    private String avatarKey;
    private String prestigeTitle;
    private List<UserAchievementDTO> achievements;
}
