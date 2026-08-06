package com.fitworkup.api.controllers;

import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        UserProfileDTO profile = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .weightKg(user.getWeightKg())
                .xp(user.getXp())
                .level(user.getLevel())
                .fitcoins(user.getFitcoins())
                .streak(user.getStreak())
                .avatarBorder(user.getAvatarBorder())
                .prestigeTitle(user.getPrestigeTitle())
                .build();

        return ResponseEntity.ok(profile);
    }
}