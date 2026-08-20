package com.fitworkup.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAvatarRequest(
        @NotBlank(message = "Selecione um avatar válido.")
        String avatarKey
) {}
