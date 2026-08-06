package com.fitworkup.service;

import com.fitworkup.dto.request.LoginRequestDTO;
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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
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
                .weightKg(request.getWeightKg() != null ? request.getWeightKg() : 70.0)
                .xp(0)
                .level(1)
                .fitcoins(0)
                .streak(0)
                .avatarBorder("DEFAULT")
                .prestigeTitle("NOVATO")
                .build();

        User savedUser = userRepository.save(newUser);

        return UserProfileDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .weightKg(savedUser.getWeightKg())
                .xp(savedUser.getXp())
                .level(savedUser.getLevel())
                .fitcoins(savedUser.getFitcoins())
                .streak(savedUser.getStreak())
                .avatarBorder(savedUser.getAvatarBorder())
                .prestigeTitle(savedUser.getPrestigeTitle())
                .build();
    }

    public JwtAuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailOrUsername(request.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication.getName());
        return new JwtAuthResponseDTO(token);
    }
}