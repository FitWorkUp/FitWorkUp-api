package com.fitworkup.service;

import com.fitworkup.dto.request.ActivityRequest;
import com.fitworkup.enums.ActivityStatus;
import com.fitworkup.security.exceptions.FraudDetectedException;
import com.fitworkup.util.FraudDetector;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    private final FraudDetector fraudDetector;

    public FraudDetectionService(FraudDetector fraudDetector) {
        this.fraudDetector = fraudDetector;
    }

    /**
     * Avalia a integridade do treino combinando checagens de servidor 
     * e os dados de risco enviados pela telemetria do Android.
     *
     * @param request Dados do treino enviado pelo app
     * @return ActivityStatus (APPROVED, UNDER_REVIEW, REJECTED)
     */
    public ActivityStatus evaluateActivity(ActivityRequest request) {
        if (request.getSteps() == null) {
            throw new FraudDetectedException("Passos não informados.");
        }

        Double distanceKm = request.getDistanceKm() != null
                ? request.getDistanceKm()
                : (request.getDistanceMeters() != null ? request.getDistanceMeters() / 1000.0 : 0.0);

        Double avgSpeed = request.getAvgSpeed() != null ? request.getAvgSpeed() : 0.0;

        if (distanceKm < 0 || avgSpeed < 0) {
            throw new FraudDetectedException("Valores de distância ou velocidade inválidos.");
        }

        if (avgSpeed > 45.0) {
            throw new FraudDetectedException("Velocidade média reportada é humanamente impossível para exercícios a pé.");
        }

        int riskScore = request.getRiskScore() != null ? request.getRiskScore() : 0;
        int heldSteps = request.getHeldSteps() != null ? request.getHeldSteps() : 0;
        int acceptedSteps = request.getAcceptedSteps() != null ? request.getAcceptedSteps() : 0;

        if (riskScore >= 5 || (acceptedSteps > 0 && heldSteps > acceptedSteps)) {
            return ActivityStatus.REJECTED;
        }

        if (riskScore >= 2 || (request.getFraudReasons() != null && !request.getFraudReasons().isEmpty())) {
            return ActivityStatus.UNDER_REVIEW;
        }

        return ActivityStatus.APPROVED;
    }
}