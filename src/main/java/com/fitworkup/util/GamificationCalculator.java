package com.fitworkup.util;

import org.springframework.stereotype.Component;

@Component
public class GamificationCalculator {
    
    private static final int BASE_XP = 100;
    private static final double EXPONENT = 1.5;

    /**
     * Calcula o XP necessário para o próximo nível usando uma curva de RPG real.
     * Fórmula: BASE_XP * (Nível ^ 1.5)
     */
    public int calculateXpForNewLevel(int currentLevel) {
        // SEGURANÇA: Evita processar níveis inválidos ou zerados
        if (currentLevel < 1) {
            return BASE_XP;
        }
        
        // Correção da multiplicação e conversão explícita para int
        return (int) (BASE_XP * Math.pow(currentLevel, EXPONENT));
    }
}