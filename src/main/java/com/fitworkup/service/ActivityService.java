package com.fitworkup.service;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.dto.response.DailySummaryResponse;
import com.fitworkup.enums.ActivityStatus;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional(readOnly = true)
    public DailySummaryResponse getTodaySummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Activity> todayActivities = activityRepository.findTodayValidActivities(userId, startOfDay);

        int totalSteps = todayActivities.stream().mapToInt(Activity::getSteps).sum();
        double totalDistanceKm = todayActivities.stream().mapToDouble(Activity::getDistanceKm).sum();

        double userWeight = (user.getWeightKg() != null && user.getWeightKg() > 0) ? user.getWeightKg() : 70.0;
        int totalCalories = (int) (totalDistanceKm * userWeight * 0.75);

        return DailySummaryResponse.builder()
                .totalSteps(totalSteps)
                .totalDistanceKm(totalDistanceKm)
                .totalCalories(totalCalories)
                .fitcoins(user.getFitcoins() != null ? user.getFitcoins() : 0)
                .xp(user.getXp() != null ? user.getXp() : 0)
                .level(user.getLevel() != null ? user.getLevel() : 1)
                .build();
    }

    @Transactional
    public Activity registerActivity(Long userId, ActivityRequest request) {
        ActivityStatus status = fraudDetectionService.evaluateActivity(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário inválido ou inexistente."));

        boolean isValid = (status == ActivityStatus.APPROVED);

        if (isValid) {
            int xpGained = (int) (request.getDistanceKm() * 100) + (request.getSteps() / 100);

            if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
                xpGained = (int) (xpGained * 1.5);
            }

            int coinsGained = (int) (request.getDistanceKm() * 10);
            gamificationService.rewardUserForActivity(userId, xpGained, coinsGained);
        }

        String verificationMethod = "GPS_TELEMETRY";
        if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
            verificationMethod = "WEARABLE_BIOMETRIC_METAS";
        } else if (request.getAvgHeartRate() != null) {
            verificationMethod = "WEARABLE_BIOMETRIC_LIVRE";
        }

        // Converte a List<String> do request para String separada por vírgula para o banco
        String fraudReasonsCsv = "";
        if (request.getFraudReasons() != null && !request.getFraudReasons().isEmpty()) {
            fraudReasonsCsv = String.join(",", request.getFraudReasons());
        }

        Activity activity = Activity.builder()
                .user(user)
                .type(request.getType() != null ? request.getType().toUpperCase() : "CAMINHADA")
                .distanceKm(request.getDistanceKm())
                .steps(request.getSteps())
                .avgSpeed(request.getAvgSpeed())
                .timestamp(LocalDateTime.now())
                .isValid(isValid)
                .plannedExerciseSessionId(request.getPlannedExerciseSessionId())
                .avgHeartRate(request.getAvgHeartRate())
                .verificationMethod(verificationMethod)
                .acceptedSteps(request.getAcceptedSteps())
                .heldSteps(request.getHeldSteps())
                .riskScore(request.getRiskScore())
                .fraudReasons(fraudReasonsCsv)
                .build();

        return activityRepository.save(activity);
    }
}