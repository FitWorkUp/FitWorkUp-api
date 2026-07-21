package com.fitworkup.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30) // Ex: CAMINHADA, CORRIDA
    private String type;

    @Column(nullable = false, name = "distance_km")
    private Double distanceKm;

    @Column(nullable = false)
    private Integer steps;

    @Column(nullable = false, name = "avg_speed")
    private Double avgSpeed;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, name = "is_valid")
    private Boolean isValid = true;

    // --- Campos Estendidos (Integração Conexão Saúde / Wearables) ---

    @Column(name = "planned_exercise_session_id", length = 100)
    private String plannedExerciseSessionId; // Armazena a correlação do plano agendado

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate; // Frequência cardíaca média detectada pelo hardware

    @Column(nullable = false, name = "verification_method", length = 40)
    private String verificationMethod = "GPS_TELEMETRY"; // GPS_TELEMETRY, WEARABLE_BIOMETRIC_LIVRE, etc.
}