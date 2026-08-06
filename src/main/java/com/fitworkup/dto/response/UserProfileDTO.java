package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private Double weightKg; // Exposto para ser editável na tela de perfil no app
    private Integer xp;
    private Integer level;
    private Integer fitcoins; // Padronizado em minúsculo para alinhar com o JSON Android
    private Integer streak;
    private String avatarBorder;
    private String prestigeTitle;
}