package com.fitworkup.service;

import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final UserRepository userRepository;

    public GamificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void rewardUserForActivity(Long userId, int xpGained, int coinsGained) {
        // SEGURANÇA: Validação de input defensiva para impedir fraudes ou valores negativos
        if (userId == null || xpGained < 0 || coinsGained < 0) {
            throw new IllegalArgumentException("Parâmetros de recompensa inválidos ou corrompidos.");
        }

        // Busca o usuário garantindo o isolamento da transação
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para atribuição de recompensas."));

        // Inicialização segura dos atributos caso estejam nulos no banco
        int currentXp = user.getXp() != null ? user.getXp() : 0;
        int currentCoins = user.getFitcoins() != null ? user.getFitcoins() : 0;
        int currentLevel = user.getLevel() != null ? user.getLevel() : 1;

        // Atualização dos saldos
        user.setXp(currentXp + xpGained);
        user.setFitcoins(currentCoins + coinsGained);

        // Algoritmo de Level Up Progressivo
        // Suporta múltiplos pulos de nível caso o bônus biométrico da Conexão Saúde tenha sido gigante
        boolean leveledUp = false;
        while (user.getXp() >= calculateXpRequiredForNextLevel(user.getLevel())) {
            int xpRequired = calculateXpRequiredForNextLevel(user.getLevel());
            user.setXp(user.getXp() - xpRequired); // Deduz o XP consumido pelo nível
            user.setLevel(user.getLevel() + 1);    // Sobe o nível do personagem
            leveledUp = true;
        }

        if (leveledUp) {
            // TODO: No futuro, disparar um evento assíncrono para gerar uma notificação push de Level Up
            System.out.println("LOG INTERNO: Usuário " + userId + " subiu para o nível " + user.getLevel());
        }

        userRepository.save(user);
    }

    /**
     * Fórmula de Curva de Nível Exponencial Suave
     * Nível 1 para 2: 1000 XP
     * Nível 2 para 3: 1500 XP
     * Nível 3 para 4: 2000 XP, e assim por diante.
     */
    private int calculateXpRequiredForNextLevel(int currentLevel) {
        return 1000 + (currentLevel - 1) * 500;
    }
}