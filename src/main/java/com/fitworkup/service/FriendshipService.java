package com.fitworkup.service;

import com.fitworkup.dto.request.FriendshipRequestDTO;
import com.fitworkup.dto.response.FriendshipResponseDTO;
import com.fitworkup.models.Friendship;
import com.fitworkup.models.User;
import com.fitworkup.repository.FriendshipRepository;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             AchievementService achievementService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    @Transactional
    public FriendshipResponseDTO sendFriendRequest(Long currentUserId, FriendshipRequestDTO request) {
        if (currentUserId.equals(request.getFriendId())) {
            throw new IllegalArgumentException("Você não pode enviar um pedido de amizade para si mesmo.");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário remetente não encontrado."));

        User friendUser = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário destinatário não encontrado."));

        if (friendshipRepository.existsFriendshipRelation(currentUser, friendUser)) {
            throw new IllegalArgumentException("Já existe um pedido pendente ou uma amizade ativa entre estes usuários.");
        }

        Friendship friendship = Friendship.builder()
                .user(currentUser)
                .friend(friendUser)
                .status("PENDING")
                .build();

        Friendship saved = friendshipRepository.save(friendship);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public FriendshipResponseDTO acceptFriendRequest(Long currentUserId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de amizade não encontrada."));

        if (!friendship.getFriend().getId().equals(currentUserId)) {
            throw new SecurityException("Apenas o destinatário do convite pode aceitar o pedido de amizade.");
        }

        friendship.setStatus("ACCEPTED");
        Friendship updated = friendshipRepository.save(friendship);
        achievementService.evaluateAllAchievements(friendship.getUser().getId());
        achievementService.evaluateAllAchievements(friendship.getFriend().getId());
        return mapToResponseDTO(updated);
    }

    @Transactional
    public void rejectFriendRequest(Long currentUserId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de amizade não encontrada."));
        if (!friendship.getFriend().getId().equals(currentUserId) || !"PENDING".equals(friendship.getStatus())) {
            throw new SecurityException("Apenas o destinatário pode rejeitar uma solicitação pendente.");
        }
        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void removeFriendship(Long currentUserId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Amizade não encontrada."));
        boolean participant = friendship.getUser().getId().equals(currentUserId)
                || friendship.getFriend().getId().equals(currentUserId);
        if (!participant) {
            throw new SecurityException("O usuário não participa desta amizade.");
        }
        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponseDTO> getPendingRequests(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return friendshipRepository.findByFriendAndStatus(currentUser, "PENDING")
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendshipResponseDTO> getFriendsList(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return friendshipRepository.findAllAcceptedFriendships(currentUser)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private FriendshipResponseDTO mapToResponseDTO(Friendship friendship) {
        return FriendshipResponseDTO.builder()
                .id(friendship.getId())
                .userId(friendship.getUser().getId())
                .username(friendship.getUser().getUsername())
                .userLevel(friendship.getUser().getLevel())
                .userAvatarKey(avatarKeyOf(friendship.getUser()))
                .friendId(friendship.getFriend().getId())
                .friendUsername(friendship.getFriend().getUsername())
                .friendLevel(friendship.getFriend().getLevel())
                .friendAvatarKey(avatarKeyOf(friendship.getFriend()))
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

    private String avatarKeyOf(User user) {
        String avatarKey = user.getAvatarKey();
        return avatarKey == null || avatarKey.isBlank() ? "ICONMAN1" : avatarKey;
    }
}
