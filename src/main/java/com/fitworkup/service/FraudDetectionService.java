package com.fitworkup.service;

import com.fitworkup.security.exceptions.FraudDetectedException;
import com.fitworkup.util.FraudDetector;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    private final FraudDetector fraudDetector;

    public FraudDetectionService(FraudDetector fraudDetector) {
        this.fraudDetector = fraudDetector;
    }

    public void validateActivityData(String activityType, Double distanceKm, Integer steps, Double avgSpeed) {
        // 1. Validar integridade básica dos dados de entrada
        if (distanceKm == null || steps == null || avgSpeed == null) {
            throw new FraudDetectedException("Dados de telemetria do treino incompletos.");
        }

        // 2. Checagem de velocidade máxima (Evita o cara fingir que está correndo dentro de um carro)
        if (fraudDetector.isSpeedImpossible(avgSpeed)) {
            throw new FraudDetectedException("Velocidade média reportada é humanamente impossível para exercícios a pé.");
        }

        // 3. Checagem de correlação (Evita injetar 999.999 passos em uma distância pequena)
        if (fraudDetector.isStrideLengthImpossible(distanceKm, steps)) {
            throw new FraudDetectedException("A relação entre passos e distância percorrida quebra as leis da biomecânica.");
        }
    }
}