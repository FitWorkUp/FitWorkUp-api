package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingItemDTO {
    private Long userId;
    private Integer position;
    private String username;
    private Integer level;
    private Long validatedSteps;
    private Long movementPoints;
    private Integer activeDays;
    private String avatarBorder;
    private String avatarKey;
    private String prestigeTitle;
    private Boolean currentUser;
}
