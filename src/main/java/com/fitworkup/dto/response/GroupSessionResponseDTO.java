package com.fitworkup.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GroupSessionResponseDTO(
        Long id,
        String code,
        String name,
        Double targetDistanceKm,
        Integer maxParticipants,
        boolean friendsOnly,
        String status,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        boolean currentUserHost,
        boolean currentUserParticipant,
        boolean currentUserReady,
        List<GroupParticipantResponseDTO> participants
) {}
