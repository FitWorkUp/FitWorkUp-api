package com.fitworkup.service;

import com.fitworkup.dto.request.ForgotPasswordRequestDTO;
import com.fitworkup.dto.request.ResetPasswordRequestDTO;
import com.fitworkup.models.PasswordResetToken;
import com.fitworkup.models.User;
import com.fitworkup.repository.PasswordResetTokenRepository;
import com.fitworkup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int EXPIRATION_MINUTES = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestReset(ForgotPasswordRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(email);

        // A resposta pública é sempre a mesma para não revelar quais e-mails estão cadastrados.
        if (userOptional.isEmpty()) return;

        User user = userOptional.get();
        tokenRepository.deleteByUser(user);

        String code = "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .codeHash(hash(email, code))
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .failedAttempts(0)
                .build();
        try {
            emailService.sendPasswordResetCode(email, code);
            tokenRepository.save(token);
        } catch (RuntimeException exception) {
            // Mantém a mesma resposta pública para e-mails conhecidos ou desconhecidos.
            LOGGER.error("Falha ao enviar código de recuperação para o e-mail solicitado.", exception);
        }
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void resetPassword(ResetPasswordRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(this::invalidCode);
        PasswordResetToken token = tokenRepository.findByUser(user)
                .orElseThrow(this::invalidCode);

        if (token.getExpiresAt().isBefore(LocalDateTime.now())
                || token.getFailedAttempts() >= MAX_ATTEMPTS) {
            tokenRepository.delete(token);
            throw invalidCode();
        }

        String receivedHash = hash(email, request.getCode());
        boolean matches = MessageDigest.isEqual(
                token.getCodeHash().getBytes(StandardCharsets.UTF_8),
                receivedHash.getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {
            token.setFailedAttempts(token.getFailedAttempts() + 1);
            tokenRepository.save(token);
            throw invalidCode();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.delete(token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String hash(String email, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((email + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível.", exception);
        }
    }

    private IllegalArgumentException invalidCode() {
        return new IllegalArgumentException("Código inválido, expirado ou com muitas tentativas.");
    }
}
