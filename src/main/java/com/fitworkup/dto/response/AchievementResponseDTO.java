package com.fitworkup.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String iconName;
    private Integer xpReward;

    @JsonProperty("fitcoinsReward")
    private Integer fitCoinsReward;
}