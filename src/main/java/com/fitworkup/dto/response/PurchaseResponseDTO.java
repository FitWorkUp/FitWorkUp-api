package com.fitworkup.dto.response;

import java.time.Instant;

public record PurchaseResponseDTO(
        Long inventoryItemId,
        Long storeItemId,
        Integer quantity,
        Integer remainingFitcoins,
        String message,
        Boolean repeatable,
        Instant boostExpiresAt
) {}
