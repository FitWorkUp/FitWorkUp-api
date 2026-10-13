package com.fitworkup.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final RestClient restClient;

    @Value("${brevo.api.key:}")
    private String apiKey;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://api.brevo.com/v3").build();
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        // Validação preventiva se a chave foi carregada
        if (apiKey == null || apiKey.isBlank()) {
            log.error("[Brevo API Error] A chave BREVO_API_KEY está vazia ou não foi carregada no Spring!");
            throw new IllegalStateException("A chave de API do Brevo não foi configurada nas variáveis de ambiente.");
        }

        // Log seguro para conferir o formato da chave (apenas os primeiros caracteres)
        String maskedKey = apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "INVÁLIDA";
        log.info("[Brevo API] Iniciando envio para: {}. Chave utilizada: {}", toEmail, maskedKey);

        try {
            Map<String, Object> body = Map.of(
                "sender", Map.of(
                    "name", "FitWorkUp",
                    "email", "20222tadssaj0007@ifba.edu.br" // E-mail cadastrado/validado no Brevo
                ),
                "to", List.of(
                    Map.of("email", toEmail)
                ),
                "subject", "Código de Recuperação - FitWorkUp",
                "htmlContent", "<html><body>" +
                        "<h2>Recuperação de Senha</h2>" +
                        "<p>Seu código de verificação é: <strong>" + code + "</strong></p>" +
                        "</body></html>"
            );

            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey.trim()) // .trim() remove espaços acidentais
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[Brevo API] E-mail enviado com sucesso para {}", toEmail);

        } catch (HttpClientErrorException e) {
            log.error("[Brevo API Error] Status: {} - Corpo da resposta: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("Falha de autenticação ou parâmetro inválido na API do Brevo: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("[Brevo API Error] Erro inesperado ao enviar e-mail", e);
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação via Brevo API.", e);
        }
    }
}