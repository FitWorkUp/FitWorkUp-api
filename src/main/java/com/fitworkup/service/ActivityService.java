package com.fitworkup.service;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;
    private final GamificationService gamificationService;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           FraudDetectionService fraudDetectionService,
                           GamificationService gamificationService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.gamificationService = gamificationService;
    }

    @Transactional
    public Activity registerActivity(Long userId, ActivityRequest request) {
        // 1. Aciona a auditoria física biomecânica (Gargalo anti-cheat padrão)
        fraudDetectionService.validateActivityData(
            request.getType(),
            request.getDistanceKm(),
            request.getSteps(),
            request.getAvgSpeed()
        );

        // 2. Busca o usuário para associar ao registro
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido ou inexistente."));

        // 3. Fórmula Algorítmica de Recompensa Base
        int xpGained = (int) (request.getDistanceKm() * 50) + (request.getSteps() / 100);

        // Mapeamento do método de verificação para fins de auditoria e segurança
        String verificationMethod = "GPS_TELEMETRY";

        // 4. Mecanismo de Bônus Biométrico Opcional (Wearable Integration)
        if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
            // Aplica multiplicador de 1.5x se cumpriu as metas cardíacas/de esforço planejadas
            xpGained = (int) (xpGained * 1.5);
            verificationMethod = "WEARABLE_BIOMETRIC_METAS";
        } else if (request.getAvgHeartRate() != null) {
            verificationMethod = "WEARABLE_BIOMETRIC_LIVRE";
        }

        int coinsGained = (int) (xpGained * 0.2);

        // 5. Injeta as moedas e o XP rodando as checagens automáticas de nível e conquistas
        gamificationService.rewardUserForActivity(userId, xpGained, coinsGained);

        // 6. Monta a entidade para gravação histórica
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setType(request.getType().toUpperCase());
        activity.setDistanceKm(request.getDistanceKm());
        activity.setSteps(request.getSteps());
        activity.setAvgSpeed(request.getAvgSpeed());
        activity.setTimestamp(LocalDateTime.now());
        activity.setIsValid(true);
        
        // Novos metadados salvos na entidade (Garanta que esses campos existam no seu modelo 'Activity')
        activity.setPlannedExerciseSessionId(request.getPlannedExerciseSessionId());
        activity.setAvgHeartRate(request.getAvgHeartRate());
        activity.setVerificationMethod(verificationMethod);

        return activityRepository.save(activity);
    }
}