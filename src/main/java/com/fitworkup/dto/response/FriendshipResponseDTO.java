package com.fitworkup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendshipResponseDTO {

    private Long id;
    private Long userId;
    private String username;
    private Integer userLevel;
    private String userAvatarKey;
    private Long friendId;
    private String friendUsername;
    private Integer friendLevel;
    private String friendAvatarKey;
    private String status;
    private LocalDateTime createdAt;
}
