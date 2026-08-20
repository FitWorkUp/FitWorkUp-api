package com.fitworkup.api.controllers;

import com.fitworkup.dto.request.CreateGroupSessionRequest;
import com.fitworkup.dto.request.JoinGroupSessionRequest;
import com.fitworkup.dto.response.GroupSessionResponseDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.GroupSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupSessionController {

    private final GroupSessionService groupSessionService;
    private final UserRepository userRepository;

    public GroupSessionController(GroupSessionService groupSessionService,
                                  UserRepository userRepository) {
        this.groupSessionService = groupSessionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<GroupSessionResponseDTO> create(
            Authentication authentication,
            @Valid @RequestBody CreateGroupSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupSessionService.create(currentUserId(authentication), request));
    }

    @PostMapping("/join")
    public ResponseEntity<GroupSessionResponseDTO> join(
            Authentication authentication,
            @Valid @RequestBody JoinGroupSessionRequest request) {
        return ResponseEntity.ok(groupSessionService.join(currentUserId(authentication), request));
    }

    @GetMapping("/{code}")
    public ResponseEntity<GroupSessionResponseDTO> get(
            Authentication authentication,
            @PathVariable String code) {
        return ResponseEntity.ok(groupSessionService.getByCode(currentUserId(authentication), code));
    }

    @PutMapping("/{code}/ready/{ready}")
    public ResponseEntity<GroupSessionResponseDTO> setReady(
            Authentication authentication,
            @PathVariable String code,
            @PathVariable boolean ready) {
        return ResponseEntity.ok(
                groupSessionService.setReady(currentUserId(authentication), code, ready)
        );
    }

    @PostMapping("/{code}/start")
    public ResponseEntity<GroupSessionResponseDTO> start(
            Authentication authentication,
            @PathVariable String code) {
        return ResponseEntity.ok(groupSessionService.start(currentUserId(authentication), code));
    }

    @DeleteMapping("/{code}/participants/me")
    public ResponseEntity<Void> leave(
            Authentication authentication,
            @PathVariable String code) {
        groupSessionService.leave(currentUserId(authentication), code);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByEmailOrUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
        return user.getId();
    }
}
