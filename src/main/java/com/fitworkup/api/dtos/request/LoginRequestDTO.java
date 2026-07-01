package com.fitworkup.api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "O identificador (e-mail ou username) é obrigatório.")
    private String login; // Pode ser e-mail ou username, dependendo de como preferir tratar no service

    @NotBlank(message = "A senha é obrigatória.")
    private String password;
}