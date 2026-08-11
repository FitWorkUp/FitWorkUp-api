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
                .mapToInt(activity -> activity.getSteps() != null ? activity.getSteps() : 0)
                .sum();
        double totalDistanceKm = todayActivities.stream()
                .mapToDouble(activity -> activity.getDistanceKm() != null ? activity.getDistanceKm() : 0.0)
                .sum();
        int totalCalories = todayActivities.stream()
                .mapToInt(activity -> activity.getCaloriesBurned() != null
                        ? activity.getCaloriesBurned()
                        : calculateCalories(user, activity.getDistanceKm(), activity.getSteps()))
                .sum();

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
        boolean isValid = status == ActivityStatus.APPROVED;

        String verificationMethod = "GPS_TELEMETRY";
        if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
            verificationMethod = "WEARABLE_BIOMETRIC_METAS";
        } else if (request.getAvgHeartRate() != null) {
            verificationMethod = "WEARABLE_BIOMETRIC_LIVRE";
        }

        String fraudReasonsCsv = request.getFraudReasons() == null
                ? ""
                : String.join(",", request.getFraudReasons());
        int caloriesBurned = calculateCalories(user, request.getDistanceKm(), request.getSteps());

        Activity activity = Activity.builder()
                .user(user)
                .type(request.getType() != null ? request.getType().toUpperCase() : "CAMINHADA")
                .distanceKm(request.getDistanceKm())
                .steps(request.getSteps())
                .caloriesBurned(caloriesBurned)
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

        Activity savedActivity = activityRepository.saveAndFlush(activity);
        if (isValid) {
            int xpGained = (int) (request.getDistanceKm() * 100) + (request.getSteps() / 100);
            if (request.getAvgHeartRate() != null && Boolean.TRUE.equals(request.getTargetsAchieved())) {
                xpGained = (int) (xpGained * 1.5);
            }
            int coinsGained = (int) (request.getDistanceKm() * 10);
            gamificationService.rewardUserForActivity(userId, xpGained, coinsGained);
            achievementService.evaluateDailyStepAchievements(userId);
        }

        return savedActivity;
    }

    private int calculateCalories(User user, Double distanceKm, Integer steps) {
        double safeDistance = distanceKm != null ? Math.max(distanceKm, 0.0) : 0.0;
        int safeSteps = steps != null ? Math.max(steps, 0) : 0;
        double weight = user.getWeightKg() != null && user.getWeightKg() > 0
                ? user.getWeightKg() : 70.0;
        int byDistance = (int) Math.round(safeDistance * weight * 0.75);
        int bySteps = (int) Math.round(safeSteps * 0.04);
        return Math.max(byDistance, bySteps);
    }
}
