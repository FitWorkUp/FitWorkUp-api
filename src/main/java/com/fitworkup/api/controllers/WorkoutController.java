package com.fitworkup.api.controllers;

import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    private final UserRepository userRepository;

    public WorkoutController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentWorkoutPlan(Principal principal) {
        User user = userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Map<String, Object> response = Map.of(
                "userId", user.getId(),
                "status", "ACTIVE",
                "message", "Plano de treino diário configurado com sucesso."
        );

        return ResponseEntity.ok(response);
    }
}