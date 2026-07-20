package com.fitworkup.util;

import org.springframework.stereotype.Component;

@Component
public class FraudDetector {

    // Limites Biológicos Humanos Baseados em Dados Reais de Desempenho
    private static final double MAX_HUMAN_SPEED_KMH = 44.72; // Velocidade máxima humana registrada (Usain Bolt)
    private static final double MIN_STRIDE_LENGTH_METERS = 0.3; // Passos curtos/caminhada lenta (30cm)
    private static final double MAX_STRIDE_LENGTH_METERS = 3.2; // Passadas gigantes de velocistas de elite (320cm)

    public boolean isSpeedImpossible(Double avgSpeed) {
        return avgSpeed != null && avgSpeed > MAX_HUMAN_SPEED_KMH;
    }

    public boolean isStrideLengthImpossible(Double distanceKm, Integer steps) {
        if (distanceKm == null || steps == null || steps <= 0 || distanceKm <= 0) {
            return true; // Dados inconsistentes, nulos ou zerados são tratados como fraude
        }

        // Converte a distância para metros para calcular o comprimento real da passada
        double distanceMeters = distanceKm * 1000.0;
        double strideLength = distanceMeters / steps;

        // SEGURANÇA: Se a passada for menor que 30cm ou maior que 3.2 metros, a física foi violada
        return strideLength < MIN_STRIDE_LENGTH_METERS || strideLength > MAX_STRIDE_LENGTH_METERS;
    }
}