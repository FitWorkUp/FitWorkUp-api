package com.fitworkup.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
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
}