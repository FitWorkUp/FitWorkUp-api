package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.dto.response.PublicUserProfileDTO;
import com.fitworkup.dto.response.UserSearchResponseDTO;
import com.fitworkup.dto.response.UserAchievementDTO;
import com.fitworkup.dto.request.UpdateAvatarRequest;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.repository.FriendshipRepository;
import com.fitworkup.service.UserService;
import com.fitworkup.service.AchievementService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AchievementService achievementService;
    private final FriendshipRepository friendshipRepository;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          AchievementService achievementService,
                          FriendshipRepository friendshipRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<UserProfileDTO> updateMyAvatar(
            Principal principal,
            @Valid @RequestBody UpdateAvatarRequest request) {
        return ResponseEntity.ok(userService.updateAvatar(principal.getName(), request.avatarKey()));
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
                        .avatarKey(user.getAvatarKey())
                        .prestigeTitle(user.getPrestigeTitle())
                        .build())
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<PublicUserProfileDTO> getPublicProfile(
            @PathVariable Long userId,
            Principal principal) {
        User currentUser = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
        User friend = userRepository.findById(userId)
                .filter(User::getActive)
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado."));

        if (!currentUser.getId().equals(friend.getId())
                && !friendshipRepository.existsAcceptedFriendship(currentUser, friend)) {
            throw new SecurityException("O perfil completo está disponível apenas para amigos.");
        }

        UserProfileDTO profile = userService.toProfile(friend);
        return ResponseEntity.ok(PublicUserProfileDTO.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .xp(profile.getXp())
                .nextLevelXp(profile.getNextLevelXp())
                .level(profile.getLevel())
                .streak(profile.getStreak())
                .totalDistanceKm(profile.getTotalDistanceKm())
                .avatarBorder(profile.getAvatarBorder())
                .avatarKey(profile.getAvatarKey())
                .prestigeTitle(profile.getPrestigeTitle())
                .achievements(achievementService.getUserAchievements(friend.getId()))
                .build());
    }
}
