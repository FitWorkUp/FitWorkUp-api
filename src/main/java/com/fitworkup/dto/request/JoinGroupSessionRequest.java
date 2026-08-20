package com.fitworkup.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record JoinGroupSessionRequest(
        @NotBlank(message = "O código da sala é obrigatório.")
        @Pattern(regexp = "(?i)^FTW-[A-Z0-9]{4}$", message = "Use um código no formato FTW-8K2P.")
        String code
) {}
