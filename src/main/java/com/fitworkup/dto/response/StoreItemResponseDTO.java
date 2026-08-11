package com.fitworkup.dto.response;

public record StoreItemResponseDTO(
        Long id,
        String name,
        String description,
        Integer price,
        String category,
        String iconEmoji,
        Boolean repeatable,
        String effectType,
        Double multiplier,
        Integer durationMinutes
) {}
