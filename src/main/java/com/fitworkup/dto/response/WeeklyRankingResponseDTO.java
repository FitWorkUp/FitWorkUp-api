package com.fitworkup.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyRankingResponseDTO {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Integer stepsPerPoint;
    private List<RankingItemDTO> entries;
}
