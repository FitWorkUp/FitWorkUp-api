package com.fitworkup.api.controllers;

import com.fitworkup.dto.request.FriendshipRequestDTO;
import com.fitworkup.dto.response.FriendshipResponseDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserRepository userRepository;

    public FriendshipController(FriendshipService friendshipService, UserRepository userRepository) {
        this.friendshipService = friendshipService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<FriendshipResponseDTO> sendFriendRequest(
            Authentication authentication,
            @Valid @RequestBody FriendshipRequestDTO request) {
        Long currentUserId = getCurrentUserId(authentication);
        FriendshipResponseDTO response = friendshipService.sendFriendRequest(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<FriendshipResponseDTO> acceptFriendRequest(
            Authentication authentication,
            @PathVariable Long id) {
        Long currentUserId = getCurrentUserId(authentication);
        FriendshipResponseDTO response = friendshipService.acceptFriendRequest(currentUserId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> rejectFriendRequest(Authentication authentication, @PathVariable Long id) {
        friendshipService.rejectFriendRequest(getCurrentUserId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFriendship(Authentication authentication, @PathVariable Long id) {
        friendshipService.removeFriendship(getCurrentUserId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipResponseDTO>> getPendingRequests(Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ResponseEntity.ok(friendshipService.getPendingRequests(currentUserId));
    }

    @GetMapping
    public ResponseEntity<List<FriendshipResponseDTO>> getFriendsList(Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        return ResponseEntity.ok(friendshipService.getFriendsList(currentUserId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailOrUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado no sistema."));
        return user.getId();
    }
}
