package com.fitworkup.dto.response;

import com.fitworkup.enums.ActivityStatus;
import com.fitworkup.models.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponse {

    private Long id;
    private String type;
    private Double distanceKm;
    private Integer steps;
    private Double avgSpeed;
    private LocalDateTime timestamp;
    private Boolean isValid;
    private ActivityStatus status;

    // Métricas de Auditoria e Anti-Fraude
    private Integer acceptedSteps;
    private Integer heldSteps;
    private Integer riskScore;
    private List<String> fraudReasons;

    // Métricas de Saúde/Wearables
    private Integer avgHeartRate;
    private String verificationMethod;

    public static ActivityResponse fromEntity(Activity activity, ActivityStatus status) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .type(activity.getType())
                .distanceKm(activity.getDistanceKm())
                .steps(activity.getSteps())
                .avgSpeed(activity.getAvgSpeed())
                .timestamp(activity.getTimestamp())
                .isValid(activity.getIsValid())
                .status(status)
                .acceptedSteps(activity.getAcceptedSteps())
                .heldSteps(activity.getHeldSteps())
                .riskScore(activity.getRiskScore())
                .fraudReasons(activity.getFraudReasons())
                .avgHeartRate(activity.getAvgHeartRate())
                .verificationMethod(activity.getVerificationMethod())
                .build();
    }
}