package com.fitworkup.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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
