package com.fitworkup.api.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;

@Data
@Table(name = "tb_workout")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double distance;

    @Column(nullable = false)
    private Integer steps;

    @Column(nullable = false)
    private Integer calories;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationsSeconds;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime = LocalDateTime.now();
}
