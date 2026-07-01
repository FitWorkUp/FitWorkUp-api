package com.fitworkup.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponseDTO {
    private String token;
    private String type = "Bearer";

    public JwtAuthResponseDTO(String token) {
        this.token = token;
    }
}