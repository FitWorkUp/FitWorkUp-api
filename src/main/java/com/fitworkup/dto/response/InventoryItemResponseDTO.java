package com.fitworkup.dto.response;

public record InventoryItemResponseDTO(
        Long id,
        Long storeItemId,
        String name,
        String description,
        Integer price,
        String category,
        String iconEmoji,
        Integer quantity,
        Boolean equipped
) {}
