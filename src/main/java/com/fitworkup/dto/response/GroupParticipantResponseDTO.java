package com.fitworkup.dto.response;

public record GroupParticipantResponseDTO(
        Long id,
        Long userId,
        String username,
        boolean ready,
        boolean host,
        boolean currentUser
) {}
