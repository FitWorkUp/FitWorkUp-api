package com.fitworkup.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupSessionRequest(
        @NotBlank(message = "O nome da sala é obrigatório.")
        @Size(max = 60, message = "O nome da sala deve ter no máximo 60 caracteres.")
        String name,

        @DecimalMin(value = "0.1", message = "A meta deve ser de pelo menos 0,1 km.")
        Double targetDistanceKm,

        @Min(value = 2, message = "A sala deve permitir pelo menos 2 participantes.")
        @Max(value = 5, message = "O limite atual é de 5 participantes.")
        Integer maxParticipants,

        Boolean friendsOnly
) {}
