package com.fitworkup.service;

import com.fitworkup.dto.request.LoginRequestDTO;
import com.fitworkup.dto.request.GoogleLoginRequestDTO;
import com.fitworkup.dto.request.RegisterRequestDTO;
import com.fitworkup.dto.response.JwtAuthResponseDTO;
import com.fitworkup.dto.response.UserProfileDTO;
import com.fitworkup.models.User;
import com.fitworkup.repository.UserRepository;
import com.fitworkup.security.Jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final JwtDecoder googleJwtDecoder;
    private final String googleClientId;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserService userService,
                       JwtDecoder googleJwtDecoder,
                       @Value("${app.security.oauth2.client.registration.google.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.googleJwtDecoder = googleJwtDecoder;
        this.googleClientId = googleClientId;
    }

    @Transactional
    public UserProfileDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("O e-mail informado já está em uso.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("O nome de usuário já está em uso.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .xp(0)
                .level(1)
                .fitcoins(0)
                .streak(0)
                .avatarBorder("DEFAULT")
                .avatarKey("ICONMAN1")
                .prestigeTitle("NOVATO")
                .build();

        User savedUser = userRepository.save(newUser);

        return userService.toProfile(savedUser);
    }

    public JwtAuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailOrUsername(request.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais invalidas."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication.getName());
        return JwtAuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInMs() / 1000)
                .user(userService.toProfile(user))
                .build();
    }

    @Transactional
    public JwtAuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        Jwt googleToken;
        try {
            googleToken = googleJwtDecoder.decode(request.getIdToken());
        } catch (JwtException ex) {
            throw new IllegalArgumentException("Assinatura ou validade do token Google não pôde ser confirmada.", ex);
        }

        String issuer = googleToken.getIssuer() != null ? googleToken.getIssuer().toString() : null;
        if (!("https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer))) {
            throw new IllegalArgumentException("Emissor do token Google inválido.");
        }
        if (!googleToken.getAudience().contains(googleClientId)) {
            throw new IllegalArgumentException("Token emitido para outro aplicativo Google.");
        }

        String googleId = googleToken.getSubject();
        String email = googleToken.getClaimAsString("email");
        Boolean emailVerified = googleToken.getClaimAsBoolean("email_verified");

        if (googleId == null || email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalArgumentException("A conta Google não possui um e-mail verificado.");
        }

        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(normalizedEmail)
                        .map(existing -> linkGoogleAccount(existing, googleId))
                        .orElseGet(() -> createGoogleUser(normalizedEmail, googleId)));

        String token = tokenProvider.generateToken(user.getEmail());
        return JwtAuthResponseDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInMs() / 1000)
                .user(userService.toProfile(user))
                .build();
    }

    private User linkGoogleAccount(User user, String googleId) {
        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleId)) {
            throw new IllegalArgumentException("Este e-mail já está vinculado a outra conta Google.");
        }
        user.setGoogleId(googleId);
        return userRepository.save(user);
    }

    private User createGoogleUser(String email, String googleId) {
        User newUser = User.builder()
                .username(createAvailableUsername(email))
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .googleId(googleId)
                .xp(0)
                .level(1)
                .fitcoins(0)
                .streak(0)
                .avatarBorder("DEFAULT")
                .avatarKey("ICONMAN1")
                .prestigeTitle("NOVATO")
                .build();
        return userRepository.save(newUser);
    }

    private String createAvailableUsername(String email) {
        String base = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "");
        if (base.isBlank()) base = "atleta";
        base = base.substring(0, Math.min(base.length(), 16));

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
