package com.fitworkup.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {
    @NotNull(message = "O tipo de atividade é obrigatório.")
    private String type;                    // EX: "CORRIDA", "CAMINHADA"

    @NotNull(message = "A distância é obrigatória.")
    @Min(value = 0, message = "A distância não pode ser negativa.")
    private Double distanceKm;              // EX: 5.2

    @NotNull(message = "A quantidade de passos é obrigatória.")
    @Min(value = 0, message = "Os passos não podem ser negativos.")
    private Integer steps;                  // EX: 6000

    @NotNull(message = "A velocidade média é obrigatória.")
    private Double avgSpeed;                // EX: 12.4

    // Campos opcionais integrados à Conexão Saúde (Smartwatches / Wearables)
    private String plannedExerciseSessionId; // ID da sessão agendada na Conexão Saúde
    private Integer avgHeartRate;            // Média de batimentos cardíacos (BPM)
    private Boolean targetsAchieved;         // Indica se o usuário cumpriu as metas do plano

    // --- Novos Campos de Auditoria Anti-Fraude ---
    @NotNull(message = "A quantidade de passos aceitos é obrigatória.")
    @Min(value = 0, message = "Passos aceitos não podem ser negativos.")
    private Integer acceptedSteps;

    @NotNull(message = "A quantidade de passos retidos é obrigatória.")
    @Min(value = 0, message = "Passos retidos não podem ser negativos.")
    private Integer heldSteps;

    @NotNull(message = "O score de risco é obrigatório.")
    @Min(value = 0, message = "O score de risco não pode ser negativo.")
    private Integer riskScore;

    @Size(max = 10, message = "O número máximo de razões de fraude registradas é 10.")
    private List<String> fraudReasons;
}