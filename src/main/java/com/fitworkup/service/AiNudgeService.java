package com.fitworkup.service;

import com.fitworkup.dto.response.DailySummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class AiNudgeService {

    public String generateNudge(DailySummaryResponse summary, String username) {
        int steps = summary.getTotalSteps() != null ? summary.getTotalSteps() : 0;

        if (steps == 0) {
            return "Olá " + username + "! Que tal dar o primeiro passo do dia? Uma pequena caminhada já ativa suas recompensas!";
        } else if (steps < 5000) {
            return "Ótimo ritmo, " + username + "! Você já registrou " + steps + " passos hoje. Falta pouco para atingir a meta recomendada de 5.000!";
        } else if (steps < 10000) {
            return "Incrível desempenho, " + username + "! Você atingiu " + steps + " passos. Mantenha o foco para conquistar mais XP!";
        } else {
            return "Lendário! Você ultrapassou os 10.000 passos hoje. Suas recompensas e ranking estão no nível máximo!";
        }
    }
}