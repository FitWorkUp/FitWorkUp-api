package com.fitworkup.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {

    @NotBlank
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Informe o código recebido.")
    @Pattern(regexp = "\\d{6}", message = "O código deve possuir 6 números.")
    private String code;

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 6, max = 72, message = "A senha deve possuir entre 6 e 72 caracteres.")
    private String newPassword;
}
