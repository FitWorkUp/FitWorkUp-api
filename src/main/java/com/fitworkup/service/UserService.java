package com.fitworkup.service;

import com.fitworkup.models.User;
import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.TokenRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final TokenRepository tokenRepository;

    public UserService(UserRepository userRepository, 
                       ActivityRepository activityRepository, 
                       TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.tokenRepository = tokenRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String identifier) {
        User user = userRepository.findByEmailOrUsername(identifier)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
        return toProfile(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO toProfile(User user) {
        int level = user.getLevel() != null ? user.getLevel() : 1;
        Double totalDistanceKm = activityRepository.sumValidDistanceByUserId(user.getId());

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .weightKg(user.getWeightKg())
                .xp(user.getXp() != null ? user.getXp() : 0)
                .nextLevelXp(level * 1000)
                .level(level)
                .fitcoins(user.getFitcoins() != null ? user.getFitcoins() : 0)
                .streak(user.getStreak() != null ? user.getStreak() : 0)
                .totalDistanceKm(totalDistanceKm != null ? totalDistanceKm : 0.0)
                .avatarBorder(user.getAvatarBorder())
                .prestigeTitle(user.getPrestigeTitle())
                .build();
    }

    @Transactional
    public void deleteAccountAndAnonymizeData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + userId));

        // 1. Apaga coordenadas sensíveis do histórico de GPS no banco (LGPD)
        activityRepository.anonymizeGpsDataByUserId(userId);

        // 2. Remove tokens e sessões ativas do usuário
        tokenRepository.deleteByUserId(userId);

        // 3. Anonimiza o perfil mantendo apenas o registro numérico desvinculado
        user.setUsername("anonimo_" + userId);
        user.setEmail("deleted_" + userId + "@deleted.fitworkup.com");
        user.setPassword("");
        user.setActive(false);

        userRepository.save(user);
    }
}
