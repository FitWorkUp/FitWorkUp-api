package com.fitworkup.service;

import com.fitworkup.enums.StoreEffectType;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserBoostRepository;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.util.GamificationCalculator;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final GamificationCalculator gamificationCalculator;
    private final UserBoostRepository userBoostRepository;

    public GamificationService(UserRepository userRepository,
                               GamificationCalculator gamificationCalculator,
                               UserBoostRepository userBoostRepository) {
        this.userRepository = userRepository;
        this.gamificationCalculator = gamificationCalculator;
        this.userBoostRepository = userBoostRepository;
    }

    @Transactional
    public void rewardUserForActivity(Long userId, int xpGained, int coinsGained) {
        rewardUser(userId, xpGained, coinsGained, true);
    }

    @Transactional
    public void rewardUserForAchievement(Long userId, int xpGained, int coinsGained) {
        rewardUser(userId, xpGained, coinsGained, false);
    }

    private void rewardUser(Long userId, int xpGained, int coinsGained, boolean applyBoosts) {
        if (userId == null || xpGained < 0 || coinsGained < 0) {
            throw new IllegalArgumentException("Parâmetros de recompensa inválidos ou inconsistentes.");
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado para atribuição de recompensas."
                ));

        int currentXp = user.getXp() != null ? user.getXp() : 0;
        int currentCoins = user.getFitcoins() != null ? user.getFitcoins() : 0;
        double xpMultiplier = applyBoosts
                ? activeMultiplier(userId, StoreEffectType.XP_MULTIPLIER) : 1.0;
        double fitcoinsMultiplier = applyBoosts
                ? activeMultiplier(userId, StoreEffectType.FITCOINS_MULTIPLIER) : 1.0;
        int finalXpGained = (int) Math.floor(xpGained * xpMultiplier);
        int finalCoinsGained = (int) Math.floor(coinsGained * fitcoinsMultiplier);

        user.setXp(currentXp + finalXpGained);
        user.setFitcoins(currentCoins + finalCoinsGained);

        while (user.getXp() >= gamificationCalculator.calculateXpForNewLevel(user.getLevel())) {
            int xpRequired = gamificationCalculator.calculateXpForNewLevel(user.getLevel());
            user.setXp(user.getXp() - xpRequired);
            user.setLevel(user.getLevel() + 1);
        }

        userRepository.save(user);
    }

    private double activeMultiplier(Long userId, StoreEffectType effectType) {
        return userBoostRepository
                .findByUserIdAndEffectTypeAndExpiresAtAfter(userId, effectType, Instant.now())
                .map(boost -> boost.getMultiplier() != null ? boost.getMultiplier() : 1.0)
                .orElse(1.0);
    }
}
