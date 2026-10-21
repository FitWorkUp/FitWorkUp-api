package com.fitworkup.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {

    @NotNull(message = "O tipo de atividade é obrigatório.")
    private String type;

    @Min(value = 0, message = "A distância não pode ser negativa.")
    private Double distanceKm;

    private Double distanceMeters;

    @NotNull(message = "A quantidade de passos é obrigatória.")
    @Min(value = 0, message = "Os passos não podem ser negativos.")
    private Integer steps;

    @NotNull(message = "A velocidade média é obrigatória.")
    private Double avgSpeed;

    private String plannedExerciseSessionId;
    private Integer avgHeartRate;
    private Boolean targetsAchieved;
    private Long groupSessionId;

    @NotNull(message = "A quantidade de passos aceitos é obrigatória.")
    @Min(value = 0, message = "Passos aceitos não podem ser negativos.")
    private Integer acceptedSteps;

    @NotNull(message = "A quantidade de passos retidos é obrigatória.")
    @Min(value = 0, message = "Passos retidos não podem ser negativos.")
    private Integer heldSteps;

    @NotNull(message = "O score de risco é obrigatório.")
    @Min(value = 0, message = "O score de risco não pode ser negativo.")
    private Integer riskScore;

    private List<String> fraudReasons;
}
