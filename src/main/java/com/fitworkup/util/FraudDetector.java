package com.fitworkup.util;

import org.springframework.stereotype.Component;

@Component
public class FraudDetector {

    // Limites Biológicos Humanos 
    private static final double MAX_HUMAN_SPEED_KMH = 45.0; 
    private static final double MIN_STRIDE_LENGTH_METERS = 0.3; // Passos muito curtos
    private static final double MAX_STRIDE_LENGTH_METERS = 2.6; // Passadas gigantes de maratonista

    public boolean isSpeedImpossible(Double avgSpeed) {
        return avgSpeed != null && avgSpeed > MAX_HUMAN_SPEED_KMH;
    }

    public boolean isStrideLengthImpossible(Double distanceKm, Integer steps) {
        if (distanceKm == null || steps == null || steps <= 0 || distanceKm <= 0) {
            return true; // Dados inconsistentes ou zerados são considerados fraudulentos
        }

        // Converte a distância para metros para calcular a passada
        double distanceMeters = distanceKm * 1000.0;
        double strideLength = distanceMeters / steps;

        // Se a passada for menor que 30cm ou maior que 2.6 metros, a física quebrou
        return strideLength < MIN_STRIDE_LENGTH_METERS || strideLength > MAX_STRIDE_LENGTH_METERS;
    }
}