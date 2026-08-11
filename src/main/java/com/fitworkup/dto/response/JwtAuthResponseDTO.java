package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthResponseDTO {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserProfileDTO user;
}
