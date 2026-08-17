package com.fitworkup.service;

import com.fitworkup.dto.request.ForgotPasswordRequestDTO;
import com.fitworkup.dto.request.ResetPasswordRequestDTO;
import com.fitworkup.models.PasswordResetToken;
import com.fitworkup.models.User;
import com.fitworkup.repository.PasswordResetTokenRepository;
import com.fitworkup.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailService
        );
    }

    @Test
    void shouldGenerateCodeAndReplacePassword() {
        User user = User.builder()
                .id(1L)
                .email("ronaldo@fitworkup.com")
                .username("ronaldo")
                .password("old-hash")
                .build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ForgotPasswordRequestDTO forgotRequest = new ForgotPasswordRequestDTO();
        forgotRequest.setEmail(" Ronaldo@FitWorkUp.com ");
        service.requestReset(forgotRequest);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendPasswordResetCode(eq(user.getEmail()), codeCaptor.capture());

        String code = codeCaptor.getValue();
        PasswordResetToken token = tokenCaptor.getValue();
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("new-hash");

        ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO();
        resetRequest.setEmail(user.getEmail());
        resetRequest.setCode(code);
        resetRequest.setNewPassword("novaSenha123");
        service.resetPassword(resetRequest);

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void shouldCountInvalidAttemptsWithoutChangingPassword() {
        User user = User.builder().id(1L).email("user@test.com").password("old-hash").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .codeHash("not-the-received-code")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail(user.getEmail());
        request.setCode("123456");
        request.setNewPassword("novaSenha123");

        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(request));
        assertEquals(1, token.getFailedAttempts());
        verify(tokenRepository).save(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldNotRevealUnknownEmailAndShouldNotSendMessage() {
        when(userRepository.findByEmail("desconhecido@test.com")).thenReturn(Optional.empty());
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("desconhecido@test.com");

        service.requestReset(request);

        verify(emailService, never()).sendPasswordResetCode(any(), any());
        verify(tokenRepository, never()).save(any());
    }
}
