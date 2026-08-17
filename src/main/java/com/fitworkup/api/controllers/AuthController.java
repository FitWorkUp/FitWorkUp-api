package com.fitworkup.api.controllers;

import com.fitworkup.dto.request.LoginRequestDTO;
import com.fitworkup.dto.request.ForgotPasswordRequestDTO;
import com.fitworkup.dto.request.RegisterRequestDTO;
import com.fitworkup.dto.request.ResetPasswordRequestDTO;
import com.fitworkup.dto.response.JwtAuthResponseDTO;
import com.fitworkup.dto.response.MessageResponseDTO;
import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.service.AuthService;
import com.fitworkup.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserProfileDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        JwtAuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<MessageResponseDTO> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(new MessageResponseDTO(
                "Se o e-mail estiver cadastrado, enviaremos um código de recuperação."
        ));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO("Senha redefinida com sucesso."));
    }
}
