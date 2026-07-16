package com.fitworkup.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingItemDTO {
    private String username;
    private Integer level;
    private Integer xp;
}