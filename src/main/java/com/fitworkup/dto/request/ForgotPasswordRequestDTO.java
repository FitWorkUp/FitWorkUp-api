package com.fitworkup.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Informe o e-mail cadastrado.")
    @Email(message = "Informe um e-mail válido.")
    private String email;
}
