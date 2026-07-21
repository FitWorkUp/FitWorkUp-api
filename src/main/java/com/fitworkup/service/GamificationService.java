package com.fitworkup.service;

import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.util.GamificationCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final GamificationCalculator gamificationCalculator;

    // Injeção de dependência limpa via construtor (Boa prática do Spring)
    public GamificationService(UserRepository userRepository, GamificationCalculator gamificationCalculator) {
        this.userRepository = userRepository;
        this.gamificationCalculator = gamificationCalculator;
    }

    @Transactional
    public void rewardUserForActivity(Long userId, int xpGained, int coinsGained) {
        // SEGURANÇA: Bloqueia valores negativos ou IDs nulos antes de tocar no banco
        if (userId == null || xpGained < 0 || coinsGained < 0) {
            throw new IllegalArgumentException("Parâmetros de recompensa inválidos ou inconsistentes.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para atribuição de recompensas."));

        // Inicialização defensiva contra valores nulos nas colunas
        int currentXp = user.getXp() != null ? user.getXp() : 0;
        int currentCoins = user.getFitcoins() != null ? user.getFitcoins() : 0;
        
        // Atualiza os saldos brutos recebidos da atividade (incluindo possíveis bônus da Conexão Saúde)
        user.setXp(currentXp + xpGained);
        user.setFitcoins(currentCoins + coinsGained);

        boolean leveledUp = false;

        // O loop avalia se o XP acumulado ultrapassa a barreira do nível atual gerada pelo Calculator
        while (user.getXp() >= gamificationCalculator.calculateXpForNewLevel(user.getLevel())) {
            int xpRequired = gamificationCalculator.calculateXpForNewLevel(user.getLevel());
            
            user.setXp(user.getXp() - xpRequired); // Desconta o custo do nível atual
            user.setLevel(user.getLevel() + 1);    // Efetiva a subida de nível
            leveledUp = true;
        }

        if (leveledUp) {
            // Log interno de auditoria. Próximo passo lógico: criar uma tabela de eventos/notificações
            System.out.println("LOG IN-GAME: Usuário " + userId + " alcançou o nível " + user.getLevel());
        }

        userRepository.save(user);
    }
}