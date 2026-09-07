package com.fitworkup.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequestDTO {

    @NotBlank(message = "O token do Google é obrigatório.")
    private String idToken;
}
