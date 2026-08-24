package com.fitworkup.config;

import com.fitworkup.models.Achievement;
import com.fitworkup.repository.AchievementRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.service.AchievementService;
import java.util.Optional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class AchievementDataConfig implements ApplicationRunner {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public AchievementDataConfig(AchievementRepository achievementRepository,
                                 UserRepository userRepository,
                                 AchievementService achievementService) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    @Override
    public void run(ApplicationArguments args) {
        save("C01", "Primeiro Passo", "Conclua a primeira atividade válida.",
                AchievementService.VALID_ACTIVITY_COUNT, 1, 50, 5, null);
        save("C02", "Rota Confirmada", "Finalize um treino com GPS, deslocamento e passos validados.",
                AchievementService.VALIDATED_ROUTE_COUNT, 1, 75, 5, null);
        save("C03", "Mil Passos", "Registre 1.000 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 1_000, 50, 5, null);
        save("C04", "Em Movimento", "Registre 5.000 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 5_000, 100, 10, null);
        save("C05", "Meta dos 10 Mil", "Registre 10.000 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 10_000, 200, 20, null);
        save("C06", "Três Dias Ativos", "Conclua atividades válidas em três dias diferentes.",
                AchievementService.ACTIVE_DAYS_TOTAL, 3, 100, 10, null);
        save("C07", "Semana Ativa", "Treine em cinco dias diferentes na mesma semana.",
                AchievementService.ACTIVE_DAYS_IN_WEEK, 5, 200, 20, null);
        save("C08", "Sequência Inicial", "Mantenha três dias consecutivos de atividade válida.",
                AchievementService.STREAK_DAYS, 3, 100, 10, null);
        save("C09", "Sete sem Parar", "Mantenha sete dias consecutivos de atividade válida.",
                AchievementService.STREAK_DAYS, 7, 250, 25, "IMPARÁVEL");
        save("C10", "Dez Treinos", "Complete dez atividades válidas.",
                AchievementService.VALID_ACTIVITY_COUNT, 10, 150, 15, null);
        save("C11", "Primeiros 10 km", "Acumule 10 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 10, 150, 15, null);
        save("C12", "Primeiro Amigo", "Tenha sua primeira amizade aceita.",
                AchievementService.ACCEPTED_FRIENDS, 1, 50, 5, null);

        save("C13", "Aquecimento", "Registre 2.500 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 2_500, 75, 5, null);
        save("C14", "Passo Firme", "Registre 7.500 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 7_500, 150, 15, null);
        save("C15", "Caminhada Épica", "Registre 15.000 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 15_000, 300, 30, null);
        save("C16", "Explorador Incansável", "Registre 20.000 passos válidos em um dia.",
                AchievementService.DAILY_STEPS, 20_000, 400, 40, null);
        save("C17", "Cem Mil Passos", "Acumule 100.000 passos válidos.",
                AchievementService.TOTAL_STEPS, 100_000, 300, 30, null);
        save("C18", "Um Milhão de Passos", "Acumule 1.000.000 de passos válidos.",
                AchievementService.TOTAL_STEPS, 1_000_000, 1_500, 150, null);

        save("C19", "Primeiro Quilômetro", "Acumule 1 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 1, 50, 5, null);
        save("C20", "Caminhante Iniciante", "Acumule 5 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 5, 100, 10, null);
        save("C21", "Explorador", "Acumule 25 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 25, 250, 25, null);
        save("C22", "Viajante", "Acumule 50 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 50, 400, 40, null);
        save("C23", "Centenário", "Acumule 100 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 100, 750, 75, null);
        save("C24", "Longa Jornada", "Acumule 500 km em atividades válidas.",
                AchievementService.TOTAL_DISTANCE_KM, 500, 2_000, 200, null);

        save("C25", "Criando o Hábito", "Mantenha três dias consecutivos de atividade válida.",
                AchievementService.STREAK_DAYS, 3, 100, 10, null);
        save("C27", "Quinzena Ativa", "Mantenha quinze dias consecutivos de atividade válida.",
                AchievementService.STREAK_DAYS, 15, 500, 50, null);
        save("C28", "Mês em Movimento", "Mantenha trinta dias consecutivos de atividade válida.",
                AchievementService.STREAK_DAYS, 30, 1_000, 100, null);
        save("C29", "Retorno Triunfal", "Volte a treinar após pelo menos sete dias sem atividade.",
                AchievementService.RETURN_AFTER_INACTIVE_DAYS, 7, 150, 15, null);
        save("C30", "Quatro Semanas Ativas", "Treine ao menos três dias em quatro semanas consecutivas.",
                AchievementService.CONSECUTIVE_ACTIVE_WEEKS, 4, 600, 60, null);

        userRepository.findAll().forEach(user ->
                achievementService.evaluateAllAchievements(user.getId())
        );
    }

    private void save(String code,
                      String name,
                      String description,
                      String criteriaType,
                      int targetValue,
                      int xpReward,
                      int fitCoinsReward,
                      String titleReward) {
        Achievement achievement = achievementRepository.findByCode(code)
                .or(() -> achievementRepository.findByName(name))
                .or(() -> legacyAchievement(criteriaType, targetValue))
                .orElseGet(Achievement::new);
        achievement.setCode(code);
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setCriteriaType(criteriaType);
        achievement.setTargetValue(targetValue);
        achievement.setXpReward(xpReward);
        achievement.setFitCoinsReward(fitCoinsReward);
        achievement.setIconName("achievement_" + code.toLowerCase());
        achievement.setTitleReward(titleReward);
        achievementRepository.save(achievement);
    }

    private Optional<Achievement> legacyAchievement(String criteriaType, int targetValue) {
        if (!AchievementService.DAILY_STEPS.equals(criteriaType)) return Optional.empty();
        return achievementRepository.findFirstByCriteriaTypeAndTargetValueAndCodeIsNull(
                criteriaType,
                targetValue
        );
    }
}
