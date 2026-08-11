package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.dto.response.UserSearchResponseDTO;
import com.fitworkup.dto.response.UserAchievementDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.UserService;
import com.fitworkup.service.AchievementService;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          AchievementService achievementService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @GetMapping("/me/achievements")
    public ResponseEntity<List<UserAchievementDTO>> getMyAchievements(Principal principal) {
        User currentUser = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
        return ResponseEntity.ok(achievementService.getUserAchievements(currentUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponseDTO>> searchUsers(
            @RequestParam String query,
            Principal principal) {
        User currentUser = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));

        List<UserSearchResponseDTO> result = userRepository
                .searchActiveUsers(query.trim(), PageRequest.of(0, 20))
                .stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> UserSearchResponseDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .level(user.getLevel())
                        .avatarBorder(user.getAvatarBorder())
                        .prestigeTitle(user.getPrestigeTitle())
                        .build())
                .toList();
        return ResponseEntity.ok(result);
    }
}
