package com.fitworkup.config;

import com.fitworkup.models.Achievement;
import com.fitworkup.repository.AchievementRepository;
import com.fitworkup.service.AchievementService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevelopmentAchievementDataConfig implements ApplicationRunner {

    private final AchievementRepository achievementRepository;

    public DevelopmentAchievementDataConfig(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("Primeiros 1.000", "Complete 1.000 passos em um dia.", 1000, 50, 5, "steps_1k");
        createIfMissing("Caminhante 5K", "Complete 5.000 passos em um dia.", 5000, 150, 15, "steps_5k");
        createIfMissing("Desafio 10K", "Complete 10.000 passos em um dia.", 10000, 300, 30, "steps_10k");
    }

    private void createIfMissing(String name,
                                 String description,
                                 int target,
                                 int xpReward,
                                 int fitCoinsReward,
                                 String iconName) {
        if (achievementRepository.existsByName(name)) return;

        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setXpReward(xpReward);
        achievement.setFitCoinsReward(fitCoinsReward);
        achievement.setIconName(iconName);
        achievement.setCriteriaType(AchievementService.DAILY_STEPS);
        achievement.setTargetValue(target);
        achievementRepository.save(achievement);
    }
}
