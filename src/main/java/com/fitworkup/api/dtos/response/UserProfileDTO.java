package com.fitworkup.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String email;
    private Integer xp;
    private Integer level;
    private Integer fitCoins;
}