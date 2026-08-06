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
        // 1. Validar integridade básica dos dados de entrada
        if (request.getDistanceKm() == null || request.getSteps() == null || request.getAvgSpeed() == null) {
            throw new FraudDetectedException("Dados de telemetria do treino incompletos.");
        }

        // 2. Checagens Biomecânicas e Físicas do Servidor
        if (fraudDetector.isSpeedImpossible(request.getAvgSpeed())) {
            throw new FraudDetectedException("Velocidade média reportada é humanamente impossível para exercícios a pé.");
        }

        if (fraudDetector.isStrideLengthImpossible(request.getDistanceKm(), request.getSteps())) {
            throw new FraudDetectedException("A relação entre passos e distância percorrida quebra as leis da biomecânica.");
        }

        // 3. Processamento dos Dados do StepFraudDetector (Telemetria do App)
        int riskScore = request.getRiskScore() != null ? request.getRiskScore() : 0;
        int heldSteps = request.getHeldSteps() != null ? request.getHeldSteps() : 0;
        int acceptedSteps = request.getAcceptedSteps() != null ? request.getAcceptedSteps() : 0;

        // Regra de Rejeição Direta: Score de risco alto (>= 5) ou proporção absurda de passos em análise
        if (riskScore >= 5 || (acceptedSteps > 0 && heldSteps > acceptedSteps)) {
            return ActivityStatus.REJECTED;
        }

        // Regra de Retenção para Análise: Score de risco moderado (>= 2) ou presença de justificativas de suspeita
        if (riskScore >= 2 || (request.getFraudReasons() != null && !request.getFraudReasons().isEmpty())) {
            return ActivityStatus.UNDER_REVIEW;
        }

        // 4. Treino validado e aprovado com sucesso
        return ActivityStatus.APPROVED;
    }
}