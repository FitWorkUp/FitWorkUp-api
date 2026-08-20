package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponseDTO {
    private Long id;
    private String username;
    private Integer level;
    private String avatarBorder;
    private String avatarKey;
    private String prestigeTitle;
}
