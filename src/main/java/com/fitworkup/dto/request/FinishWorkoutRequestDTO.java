package com.fitworkup.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinishWorkoutRequestDTO {

    @NotNull(message = "O tipo de atividade é obrigatório.")
    private String type;

    @NotNull(message = "A distância é obrigatória.")
    @Min(value = 0, message = "A distância não pode ser negativa.")
    private Double distanceKm;

    @NotNull(message = "A quantidade total de passos é obrigatória.")
    @Min(value = 0, message = "Os passos não podem ser negativos.")
    private Integer steps;

    @NotNull(message = "A velocidade média é obrigatória.")
    private Double avgSpeed;

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

    private String plannedExerciseSessionId;
    private Integer avgHeartRate;
    private Boolean targetsAchieved;
}