package com.fitworkup.service;

import com.fitworkup.dto.response.UserAchievementDTO;
import com.fitworkup.models.Achievement;
import com.fitworkup.models.User;
import com.fitworkup.models.UserAchievement;
import com.fitworkup.repository.ActivityRepository;
import com.fitworkup.repository.AchievementRepository;
import com.fitworkup.repository.UserAchievementRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService {

    public static final String DAILY_STEPS = "DAILY_STEPS";

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              ActivityRepository activityRepository,
                              UserRepository userRepository,
                              GamificationService gamificationService) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    @Transactional
    public void evaluateDailyStepAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        long todaySteps = activityRepository.sumTodayValidSteps(
                userId,
                LocalDate.now().atStartOfDay()
        );

        achievementRepository.findByCriteriaTypeOrderByTargetValueAsc(DAILY_STEPS)
                .stream()
                .filter(achievement -> achievement.getTargetValue() != null)
                .filter(achievement -> todaySteps >= achievement.getTargetValue())
                .filter(achievement -> !userAchievementRepository
                        .existsByUserIdAndAchievementId(userId, achievement.getId()))
                .forEach(achievement -> unlock(user, achievement));
    }

    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdWithDetails(userId).stream()
                .map(userAchievement -> {
                    Achievement achievement = userAchievement.getAchievement();
                    return new UserAchievementDTO(
                            achievement.getId(),
                            achievement.getName(),
                            achievement.getDescription(),
                            true,
                            userAchievement.getUnlockedAt(),
                            achievement.getIconName(),
                            achievement.getXpReward(),
                            achievement.getFitCoinsReward()
                    );
                })
                .toList();
    }

    private void unlock(User user, Achievement achievement) {
        UserAchievement unlocked = new UserAchievement(
                null,
                user,
                achievement,
                LocalDateTime.now()
        );
        userAchievementRepository.save(unlocked);
        gamificationService.rewardUserForAchievement(
                user.getId(),
                achievement.getXpReward(),
                achievement.getFitCoinsReward()
        );
    }
}
