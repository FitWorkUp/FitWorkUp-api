package com.fitworkup.service;

import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void sendWelcomeEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Bem-vindo ao FitWorkUp");
            helper.setText(
                    "<html><body>" +
                            "<h2>Olá, " + user.getUsername() + "!</h2>" +
                            "<p>Seu cadastro foi concluído com sucesso.</p>" +
                            "<p>Agora você pode começar a registrar suas atividades e acompanhar seu progresso.</p>" +
                            "</body></html>",
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de boas-vindas", e);
        }
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Código de recuperação de senha - FitWorkUp");
            helper.setText(
                    "<html><body>" +
                            "<h2>Recuperação de senha</h2>" +
                            "<p>Seu código de segurança é:</p>" +
                            "<h3>" + code + "</h3>" +
                            "<p>Este código expira em 15 minutos.</p>" +
                            "</body></html>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Erro ao enviar código de recuperação de senha.", e);
        }
    }
}