package com.fitworkup.service;

import com.fitworkup.dto.request.CreateGroupSessionRequest;
import com.fitworkup.dto.request.JoinGroupSessionRequest;
import com.fitworkup.dto.response.GroupParticipantResponseDTO;
import com.fitworkup.dto.response.GroupSessionResponseDTO;
import com.fitworkup.models.GroupParticipant;
import com.fitworkup.models.GroupSession;
import com.fitworkup.models.User;
import com.fitworkup.repository.FriendshipRepository;
import com.fitworkup.repository.GroupParticipantRepository;
import com.fitworkup.repository.GroupSessionRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.exceptions.BusinessRuleException;
import com.fitworkup.security.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class GroupSessionService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int DEFAULT_MAX_PARTICIPANTS = 5;

    private final GroupSessionRepository groupSessionRepository;
    private final GroupParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public GroupSessionService(GroupSessionRepository groupSessionRepository,
                               GroupParticipantRepository participantRepository,
                               UserRepository userRepository,
                               FriendshipRepository friendshipRepository) {
        this.groupSessionRepository = groupSessionRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    public GroupSessionResponseDTO create(Long currentUserId, CreateGroupSessionRequest request) {
        User host = requireUser(currentUserId);
        int maxParticipants = request.maxParticipants() != null
                ? request.maxParticipants()
                : DEFAULT_MAX_PARTICIPANTS;

        GroupSession session = GroupSession.builder()
                .code(generateUniqueCode())
                .name(request.name().trim())
                .host(host)
                .targetDistanceKm(request.targetDistanceKm())
                .maxParticipants(maxParticipants)
                .friendsOnly(request.friendsOnly() == null || request.friendsOnly())
                .status("LOBBY")
                .build();
        session = groupSessionRepository.save(session);

        participantRepository.save(GroupParticipant.builder()
                .groupSession(session)
                .user(host)
                .ready(true)
                .build());

        return toResponse(session, currentUserId);
    }

    @Transactional
    public GroupSessionResponseDTO join(Long currentUserId, JoinGroupSessionRequest request) {
        User user = requireUser(currentUserId);
        GroupSession session = requireByCode(request.code());
        requireLobby(session);

        if (participantRepository.existsByGroupSessionIdAndUserId(session.getId(), currentUserId)) {
            return toResponse(session, currentUserId);
        }
        if (participantRepository.countByGroupSessionId(session.getId()) >= session.getMaxParticipants()) {
            throw new BusinessRuleException("A sala já atingiu o limite de participantes.");
        }
        if (Boolean.TRUE.equals(session.getFriendsOnly()) &&
                !friendshipRepository.existsAcceptedFriendship(session.getHost(), user)) {
            throw new SecurityException("Esta sala está aberta somente para amigos do anfitrião.");
        }

        participantRepository.save(GroupParticipant.builder()
                .groupSession(session)
                .user(user)
                .ready(false)
                .build());
        return toResponse(session, currentUserId);
    }

    @Transactional(readOnly = true)
    public GroupSessionResponseDTO getByCode(Long currentUserId, String code) {
        GroupSession session = requireByCode(code);
        if (!participantRepository.existsByGroupSessionIdAndUserId(session.getId(), currentUserId)) {
            throw new SecurityException("Entre na sala antes de acessar o lobby.");
        }
        return toResponse(session, currentUserId);
    }

    @Transactional
    public GroupSessionResponseDTO setReady(Long currentUserId, String code, boolean ready) {
        GroupSession session = requireByCode(code);
        requireLobby(session);
        GroupParticipant participant = participantRepository
                .findByGroupSessionIdAndUserId(session.getId(), currentUserId)
                .orElseThrow(() -> new SecurityException("O usuário não participa desta sala."));
        participant.setReady(ready);
        participantRepository.save(participant);
        return toResponse(session, currentUserId);
    }

    @Transactional
    public GroupSessionResponseDTO start(Long currentUserId, String code) {
        GroupSession session = requireByCode(code);
        requireLobby(session);
        if (!session.getHost().getId().equals(currentUserId)) {
            throw new SecurityException("Somente o anfitrião pode iniciar a atividade.");
        }

        List<GroupParticipant> participants = participantRepository
                .findAllWithUserBySessionId(session.getId());
        if (participants.size() < 2) {
            throw new BusinessRuleException("Aguarde pelo menos mais um participante.");
        }
        if (participants.stream().anyMatch(participant -> !Boolean.TRUE.equals(participant.getReady()))) {
            throw new BusinessRuleException("Todos os participantes precisam estar prontos.");
        }

        session.setStatus("ACTIVE");
        session.setStartedAt(LocalDateTime.now());
        groupSessionRepository.save(session);
        return toResponse(session, currentUserId);
    }

    @Transactional
    public void leave(Long currentUserId, String code) {
        GroupSession session = requireByCode(code);
        requireLobby(session);
        GroupParticipant participant = participantRepository
                .findByGroupSessionIdAndUserId(session.getId(), currentUserId)
                .orElseThrow(() -> new SecurityException("O usuário não participa desta sala."));

        if (session.getHost().getId().equals(currentUserId)) {
            if (participantRepository.countByGroupSessionId(session.getId()) > 1) {
                throw new BusinessRuleException("O anfitrião não pode sair enquanto houver participantes.");
            }
            groupSessionRepository.delete(session);
        } else {
            participantRepository.delete(participant);
        }
    }

    private GroupSession requireByCode(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase(Locale.ROOT);
        return groupSessionRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada."));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private void requireLobby(GroupSession session) {
        if (!"LOBBY".equals(session.getStatus())) {
            throw new BusinessRuleException("A sala não está mais aberta para alterações.");
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder suffix = new StringBuilder(4);
            for (int index = 0; index < 4; index++) {
                suffix.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
            }
            String code = "FTW-" + suffix;
            if (!groupSessionRepository.existsByCodeIgnoreCase(code)) return code;
        }
        throw new IllegalStateException("Não foi possível gerar um código de sala único.");
    }

    private GroupSessionResponseDTO toResponse(GroupSession session, Long currentUserId) {
        List<GroupParticipant> participants = participantRepository
                .findAllWithUserBySessionId(session.getId());
        List<GroupParticipantResponseDTO> participantResponses = participants.stream()
                .map(participant -> new GroupParticipantResponseDTO(
                        participant.getId(),
                        participant.getUser().getId(),
                        participant.getUser().getUsername(),
                        Boolean.TRUE.equals(participant.getReady()),
                        session.getHost().getId().equals(participant.getUser().getId()),
                        currentUserId.equals(participant.getUser().getId())
                ))
                .toList();

        boolean currentUserParticipant = participants.stream()
                .anyMatch(participant -> currentUserId.equals(participant.getUser().getId()));
        boolean currentUserReady = participants.stream()
                .filter(participant -> currentUserId.equals(participant.getUser().getId()))
                .findFirst()
                .map(participant -> Boolean.TRUE.equals(participant.getReady()))
                .orElse(false);

        return new GroupSessionResponseDTO(
                session.getId(),
                session.getCode(),
                session.getName(),
                session.getTargetDistanceKm(),
                session.getMaxParticipants(),
                Boolean.TRUE.equals(session.getFriendsOnly()),
                session.getStatus(),
                session.getCreatedAt(),
                session.getStartedAt(),
                session.getHost().getId().equals(currentUserId),
                currentUserParticipant,
                currentUserReady,
                participantResponses
        );
    }
}
