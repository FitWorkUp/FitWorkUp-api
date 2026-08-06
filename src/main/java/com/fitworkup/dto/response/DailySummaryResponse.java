package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySummaryResponse {
    private Integer totalSteps;
    private Double totalDistanceKm;
    private Integer totalCalories;
    private Integer fitcoins;
    private Integer xp;
    private Integer level;
}