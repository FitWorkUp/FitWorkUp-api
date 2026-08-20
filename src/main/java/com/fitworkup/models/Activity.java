package com.fitworkup.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // ex: "RUNNING", "WALKING", "CYCLING"

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    private Integer durationSeconds;

    @Column(name = "group_session_id")
    private Long groupSessionId;

    private Integer caloriesBurned;

    private Integer steps;

    @Column(name = "accepted_steps")
    private Integer acceptedSteps;

    @Column(name = "held_steps")
    private Integer heldSteps;

    @Column(name = "avg_speed")
    private Double avgSpeed;

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate;

    @Column(name = "planned_exercise_session_id")
    private String plannedExerciseSessionId;

    @Getter(AccessLevel.NONE)
    @Column(name = "fraud_reasons", length = 500)
    private String fraudReasons;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "verification_method")
    private String verificationMethod; // "GPS", "PEDOMETER", "ANONYMIZED"

    @Builder.Default
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Builder.Default
    @Column(name = "risk_score")
    private Integer riskScore = 0;

    // Getter customizado que converte a String do banco em List<String> para o ActivityResponse
    public List<String> getFraudReasons() {
        if (this.fraudReasons == null || this.fraudReasons.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(this.fraudReasons.split("\\s*,\\s*"));
    }

    public void setFraudReasons(String fraudReasons) {
        this.fraudReasons = fraudReasons;
    }
}
