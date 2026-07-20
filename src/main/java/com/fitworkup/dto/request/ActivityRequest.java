package com.fitworkup.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {
    private String type;                    // EX: "CORRIDA", "CAMINHADA"
    private Double distanceKm;              // EX: 5.2
    private Integer steps;                  // EX: 6000
    private Double avgSpeed;                // EX: 12.4

    // Campos opcionais integrados à Conexão Saúde (Smartwatches / Wearables)
    private String plannedExerciseSessionId; // ID da sessão agendada na Conexão Saúde
    private Integer avgHeartRate;            // Média de batimentos cardíacos (BPM)
    private Boolean targetsAchieved;         // Indica se o usuário cumpriu as metas do plano
}