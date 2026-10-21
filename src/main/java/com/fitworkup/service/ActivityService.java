package com.fitworkup.service;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.dto.response.DailySummaryResponse;
import com.fitworkup.enums.ActivityStatus;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;
    private final GamificationService gamificationService;
    private final AchievementService achievementService;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           FraudDetectionService fraudDetectionService,
                           GamificationService gamificationService,
                           AchievementService achievementService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.gamificationService = gamificationService;
        this.achievementService = achievementService;
    }

    @Transactional(readOnly = true)
    public DailySummaryResponse getTodaySummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        List<Activity> todayActivities = activityRepository.findTodayValidActivities(
                userId,
                LocalDate.now().atStartOfDay()
        );

        int totalSteps = todayActivities.stream()
                .mapToInt(activity -> activity.getAcceptedSteps() != null
                        ? Math.max(activity.getAcceptedSteps(), 0)
                        : (activity.getSteps() != null ? Math.max(activity.getSteps(), 0) : 0))
                .sum();
        double totalDistanceKm = todayActivities.stream()
                .mapToDouble(activity -> activity.getDistanceKm() != null ? activity.getDistanceKm() : 0.0)
                .sum();

        return DailySummaryResponse.builder()
                .totalSteps(totalSteps)
                .totalDistanceKm(totalDistanceKm)
                .fitcoins(user.getFitcoins() != null ? user.getFitcoins() : 0)
                .xp(user.getXp() != null ? user.getXp() : 0)
                .level(user.getLevel() != null ? user.getLevel() : 1)
                .build();
    }

    @Transactional
    public Activity registerActivity(Long userId, ActivityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido ou inexistente."));

        // 1. Tratamento defensivo para campos que podem vir nulos em envios offline
        int steps = request.getSteps() != null ? Math.max(request.getSteps(), 0) : 0;
        double distanceKm = request.getDistanceKm() != null ? Math.max(request.getDistanceKm(), 0.0) : 0.0;
        int acceptedSteps = request.getAcceptedSteps() != null ? Math.max(request.getAcceptedSteps(), 0) : steps;
        int heldSteps = request.getHeldSteps() != null ? Math.max(request.getHeldSteps(), 0) : 0;
        int riskScore = request.getRiskScore() != null ? request.getRiskScore() : 0;

        // 2. Avaliação de fraude com suporte a dados offline e fallback físico
        boolean isValid = false;
        try {
            ActivityStatus status = fraudDetectionService.evaluateActivity(request);
            isValid = (status == ActivityStatus.APPROVED);
        } catch (Exception e) {
            isValid = false;
        }

        // Se o motor rejeitou ou falhou, mas os dados respeitam os limites físicos humanos (offline sync), aprova:
        if (!isValid && steps > 0 && riskScore < 50) {
            boolean speedOk = request.getAvgSpeed() == null || request.getAvgSpeed() <= 45.0; // Limite humano max
            boolean strideOk = true;
            
            if (distanceKm > 0) {
                double distanceMeters = distanceKm * 1000.0;
                double strideLength = distanceMeters / steps;
                strideOk = (strideLength >= 0.3 && strideLength <= 2.6); // Amplitude de passada válida
            }

            if (speedOk && strideOk) {
                isValid = true;
            }
        }

        // 3. Método de verificação
        String verificationMethod = "GPS_TELEMETRY";
        if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
            verificationMethod = "WEARABLE_BIOMETRIC_METAS";
        } else if (request.getAvgHeartRate() != null) {
            verificationMethod = "WEARABLE_BIOMETRIC_LIVRE";
        } else if (steps > 0) {
            verificationMethod = "OFFLINE_SYNC_PEDOMETER";
        }

        String fraudReasonsCsv = request.getFraudReasons() == null || request.getFraudReasons().isEmpty()
                ? ""
                : String.join(",", request.getFraudReasons());

        // 4. Construção da Atividade
        Activity activity = Activity.builder()
                .user(user)
                .type(request.getType() != null ? request.getType().toUpperCase() : "CAMINHADA")
                .distanceKm(distanceKm)
                .steps(steps)
                .groupSessionId(request.getGroupSessionId())
                .avgSpeed(request.getAvgSpeed())
                .timestamp(LocalDateTime.now())
                .isValid(isValid)
                .plannedExerciseSessionId(request.getPlannedExerciseSessionId())
                .avgHeartRate(request.getAvgHeartRate())
                .verificationMethod(verificationMethod)
                .acceptedSteps(acceptedSteps)
                .heldSteps(heldSteps)
                .riskScore(riskScore)
                .fraudReasons(fraudReasonsCsv)
                .build();

        Activity savedActivity = activityRepository.saveAndFlush(activity);
        
        // 5. Recompensas e Streak se aprovado
        if (isValid) {
            updateActivityStreak(user, savedActivity.getTimestamp().toLocalDate());
            int rewardedSteps = acceptedSteps > 0 ? acceptedSteps : steps;
            int xpGained = (int) (distanceKm * 100) + (rewardedSteps / 100);
            
            if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
                xpGained = (int) (xpGained * 1.5);
            }
            int coinsGained = (int) (distanceKm * 10);
            
            gamificationService.rewardUserForActivity(userId, xpGained, coinsGained);
            achievementService.evaluateAllAchievements(userId);
        }

        return savedActivity;
    }

    private void updateActivityStreak(User user, LocalDate activityDate) {
        LocalDate lastActivityDate = user.getLastActivityDate();

        if (lastActivityDate == null) {
            user.setStreak(1);
        } else if (lastActivityDate.equals(activityDate)) {
            return;
        } else if (lastActivityDate.equals(activityDate.minusDays(1))) {
            int currentStreak = user.getStreak() != null ? user.getStreak() : 0;
            user.setStreak(currentStreak + 1);
        } else {
            user.setStreak(1);
        }

        user.setLastActivityDate(activityDate);
        userRepository.save(user);
    }

}
