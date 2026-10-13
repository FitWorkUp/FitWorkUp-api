package com.fitworkup.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
/*public class EmailService {

    private final JavaMailSender mailSender;
    private final String senderAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String senderAddress) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
    }

    public void sendPasswordResetCode(String recipient, String code) {
        if (senderAddress == null || senderAddress.isBlank()) {
            throw new IllegalStateException("O e-mail de recuperação ainda não foi configurado no servidor.");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(senderAddress);
            helper.setTo(recipient);
            helper.setSubject("Código para redefinir sua senha - FitWorkUp");
            helper.setText("""
                    <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:24px">
                      <h2 style="color:#e5241b">Redefinição de senha</h2>
                      <p>Use o código abaixo no aplicativo FitWorkUp:</p>
                      <p style="font-size:32px;font-weight:bold;letter-spacing:8px">%s</p>
                      <p>O código expira em 15 minutos. Se você não solicitou a troca, ignore este e-mail.</p>
                    </div>
                    """.formatted(code), true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação.", exception);
        }
    }
}
*/

@Service
public class EmailService {

    private final RestClient restClient;

    @Value("${brevo.api.key}")
    private String apiKey;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://api.brevo.com/v3").build();
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            Map<String, Object> body = Map.of(
                "sender", Map.of(
                    "name", "FitWorkUp",
                    "email", "20222tadssaj0007@ifba.edu.br" // IMPORTANTE: O mesmo e-mail verificado no Brevo
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
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação via Brevo API.", e);
        }
    }
}