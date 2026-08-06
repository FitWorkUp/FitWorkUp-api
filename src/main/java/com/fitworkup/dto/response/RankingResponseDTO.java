package com.fitworkup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingResponseDTO {
    private Long id;
    private String username;
    private Integer xp;
    private Integer level;
    private String avatarBorder;
    private String prestigeTitle;
}