package com.fitworkup.api.dtos.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipResponseDTO {
    private Long id;
    private String friendUsername;
    private Integer friendLevel;
    private String status;
    private LocalDateTime createdAt;
}