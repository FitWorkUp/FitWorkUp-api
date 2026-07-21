package com.fitworkup.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipRequestDTO {

    @NotNull(message = "O ID do amigo a ser adicionado é obrigatório.")
    private Long friendId;
}