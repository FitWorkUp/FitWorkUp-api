package com.fitworkup.service;

import com.fitworkup.models.User;
import com.fitworkup.security.exceptions.UserNotFoundException;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.TokenRepository;
import com.fitworkup.repository.UserRepository;
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

    @Transactional
    public void deleteAccountAndAnonymizeData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + userId));

        // 1. Apaga coordenadas sensíveis do histórico de GPS no banco (LGPD)
        activityRepository.anonymizeGpsDataByUserId(userId);

        // 2. Remove tokens e sessões ativas do usuário
        tokenRepository.deleteByUserId(userId);

        // 3. Anonimiza o perfil mantendo apenas o registro numérico desvinculado
        user.setName("Usuário Anônimo " + userId);
        user.setEmail("deleted_" + userId + "@deleted.fitworkup.com");
        user.setPassword("");
        user.setActive(false);

        userRepository.save(user);
    }
}