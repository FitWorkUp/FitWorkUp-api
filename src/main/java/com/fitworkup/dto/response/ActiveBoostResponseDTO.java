package com.fitworkup.dto.response;

import java.time.Instant;

public record ActiveBoostResponseDTO(
        String effectType,
        Double multiplier,
        Instant expiresAt
) {}
