package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String iconName;
    private Integer xpReward;
    private Integer fitCoinsReward;
}