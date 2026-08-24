package com.fitworkup.service;

import com.fitworkup.dto.response.UserAchievementDTO;
import com.fitworkup.models.Achievement;
import com.fitworkup.models.Activity;
import com.fitworkup.models.User;
import com.fitworkup.models.UserAchievement;
import com.fitworkup.repository.AchievementRepository;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.FriendshipRepository;
import com.fitworkup.repository.UserAchievementRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.exceptions.ResourceNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService {

    public static final String VALID_ACTIVITY_COUNT = "VALID_ACTIVITY_COUNT";
    public static final String VALIDATED_ROUTE_COUNT = "VALIDATED_ROUTE_COUNT";
    public static final String DAILY_STEPS = "DAILY_STEPS";
    public static final String TOTAL_STEPS = "TOTAL_STEPS";
    public static final String ACTIVE_DAYS_TOTAL = "ACTIVE_DAYS_TOTAL";
    public static final String ACTIVE_DAYS_IN_WEEK = "ACTIVE_DAYS_IN_WEEK";
    public static final String STREAK_DAYS = "STREAK_DAYS";
    public static final String TOTAL_DISTANCE_KM = "TOTAL_DISTANCE_KM";
    public static final String ACCEPTED_FRIENDS = "ACCEPTED_FRIENDS";
    public static final String RETURN_AFTER_INACTIVE_DAYS = "RETURN_AFTER_INACTIVE_DAYS";
    public static final String CONSECUTIVE_ACTIVE_WEEKS = "CONSECUTIVE_ACTIVE_WEEKS";

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ActivityRepository activityRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              ActivityRepository activityRepository,
                              FriendshipRepository friendshipRepository,
                              UserRepository userRepository,
                              GamificationService gamificationService) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.activityRepository = activityRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    /** Reavalia o catálogo após uma atividade ou amizade aceita. */
    @Transactional
    public void evaluateAllAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        AchievementStats stats = calculateStats(userId);

        for (Achievement achievement : achievementRepository.findAll()) {
            if (achievement.getTargetValue() == null || achievement.getCriteriaType() == null) continue;
            if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) continue;
            if (progressFor(achievement.getCriteriaType(), stats) >= achievement.getTargetValue()) {
                unlock(user, achievement);
            }
        }
    }

    /** Mantido para não quebrar chamadas existentes. */
    @Transactional
    public void evaluateDailyStepAchievements(Long userId) {
        evaluateAllAchievements(userId);
    }

    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdWithDetails(userId).stream()
                .map(userAchievement -> {
                    Achievement achievement = userAchievement.getAchievement();
                    return new UserAchievementDTO(
                            achievement.getId(), achievement.getName(), achievement.getDescription(),
                            true, userAchievement.getUnlockedAt(), achievement.getIconName(),
                            achievement.getXpReward(), achievement.getFitCoinsReward()
                    );
                })
                .toList();
    }

    private AchievementStats calculateStats(Long userId) {
        List<Activity> activities = activityRepository
                .findByUserIdAndIsValidTrueOrderByTimestampAsc(userId);
        Map<LocalDate, Long> stepsByDay = activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getTimestamp().toLocalDate(),
                        Collectors.summingLong(this::acceptedSteps)
                ));
        Set<LocalDate> activeDays = new TreeSet<>(stepsByDay.keySet());
        Map<LocalDate, Long> activeDaysByWeek = activeDays.stream()
                .map(this::startOfWeek)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new AchievementStats(
                activities.size(),
                activities.stream().filter(this::hasValidatedRoute).count(),
                stepsByDay.values().stream().mapToLong(Long::longValue).max().orElse(0),
                stepsByDay.values().stream().mapToLong(Long::longValue).sum(),
                activities.stream().map(Activity::getDistanceKm)
                        .filter(value -> value != null && value > 0)
                        .mapToDouble(Double::doubleValue).sum(),
                activeDays.size(),
                activeDaysByWeek.values().stream().mapToLong(Long::longValue).max().orElse(0),
                maxConsecutiveDates(activeDays),
                maxInactiveDaysBeforeReturn(activeDays),
                maxConsecutiveActiveWeeks(activeDaysByWeek),
                friendshipRepository.countAcceptedFriendships(userId)
        );
    }

    private double progressFor(String criteriaType, AchievementStats stats) {
        return switch (criteriaType) {
            case VALID_ACTIVITY_COUNT -> stats.validActivityCount();
            case VALIDATED_ROUTE_COUNT -> stats.validatedRouteCount();
            case DAILY_STEPS -> stats.maxDailySteps();
            case TOTAL_STEPS -> stats.totalSteps();
            case ACTIVE_DAYS_TOTAL -> stats.activeDaysTotal();
            case ACTIVE_DAYS_IN_WEEK -> stats.maxActiveDaysInWeek();
            case STREAK_DAYS -> stats.maxStreakDays();
            case TOTAL_DISTANCE_KM -> stats.totalDistanceKm();
            case ACCEPTED_FRIENDS -> stats.acceptedFriends();
            case RETURN_AFTER_INACTIVE_DAYS -> stats.maxInactiveDaysBeforeReturn();
            case CONSECUTIVE_ACTIVE_WEEKS -> stats.maxConsecutiveActiveWeeks();
            default -> 0;
        };
    }

    private long acceptedSteps(Activity activity) {
        Integer value = activity.getAcceptedSteps() != null ? activity.getAcceptedSteps() : activity.getSteps();
        return value != null ? Math.max(value, 0) : 0;
    }

    private boolean hasValidatedRoute(Activity activity) {
        String verification = activity.getVerificationMethod();
        return verification != null && verification.toUpperCase().contains("GPS")
                && acceptedSteps(activity) > 0
                && activity.getDistanceKm() != null && activity.getDistanceKm() > 0;
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private long maxConsecutiveDates(Set<LocalDate> dates) {
        LocalDate previous = null;
        long current = 0;
        long maximum = 0;
        for (LocalDate date : dates) {
            current = previous != null && previous.plusDays(1).equals(date) ? current + 1 : 1;
            maximum = Math.max(maximum, current);
            previous = date;
        }
        return maximum;
    }

    private long maxInactiveDaysBeforeReturn(Set<LocalDate> dates) {
        LocalDate previous = null;
        long maximum = 0;
        for (LocalDate date : dates) {
            if (previous != null) maximum = Math.max(maximum, ChronoUnit.DAYS.between(previous, date) - 1);
            previous = date;
        }
        return maximum;
    }

    private long maxConsecutiveActiveWeeks(Map<LocalDate, Long> activeDaysByWeek) {
        Set<LocalDate> qualifyingWeeks = activeDaysByWeek.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        LocalDate previous = null;
        long current = 0;
        long maximum = 0;
        for (LocalDate week : qualifyingWeeks) {
            current = previous != null && previous.plusWeeks(1).equals(week) ? current + 1 : 1;
            maximum = Math.max(maximum, current);
            previous = week;
        }
        return maximum;
    }

    private void unlock(User user, Achievement achievement) {
        userAchievementRepository.save(new UserAchievement(null, user, achievement, LocalDateTime.now()));
        gamificationService.rewardUserForAchievement(
                user.getId(), achievement.getXpReward(), achievement.getFitCoinsReward()
        );
        if (achievement.getTitleReward() != null && !achievement.getTitleReward().isBlank()
                && (user.getPrestigeTitle() == null || "NOVATO".equalsIgnoreCase(user.getPrestigeTitle()))) {
            user.setPrestigeTitle(achievement.getTitleReward());
            userRepository.save(user);
        }
    }

    private record AchievementStats(
            long validActivityCount,
            long validatedRouteCount,
            long maxDailySteps,
            long totalSteps,
            double totalDistanceKm,
            long activeDaysTotal,
            long maxActiveDaysInWeek,
            long maxStreakDays,
            long maxInactiveDaysBeforeReturn,
            long maxConsecutiveActiveWeeks,
            long acceptedFriends
    ) {}
}
