package com.fitworkup.services;

import com.fitworkup.dtos.request.RegisterRequestDTO;
import com.fitworkup.dtos.response.UserProfileDTO;
import com.fitworkup.exceptions.BusinessRuleException;
import com.fitworkup.models.User;
import com.fitworkup.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Em produção, este bean deve vir de uma classe de configuração de segurança
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserProfileDTO registrarUsuario(RegisterRequestDTO dto) {
        // 1. Validação de Segurança: Verificar duplicidade de dados (OWASP Top 10 / LGPD)
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("E-mail já cadastrado no sistema.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("Nome de usuário já está em uso.");
        }

        // 2. Criação da Entidade com Princípios de Segurança e Inicialização Gamificada
        User novoUsuario = new User();
        novoUsuario.setUsername(dto.getUsername());
        novoUsuario.setEmail(dto.getEmail());
        
        // Criptografia em repouso da senha do usuário
        novoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Status inicial do jogo
        novoUsuario.setXp(0);
        novoUsuario.setLevel(1);
        novoUsuario.setFitcoins(0);

        // 3. Persistência no PostgreSQL via Repository
        User usuarioSalvo = userRepository.save(novoUsuario);

        // 4. Retorno Limpo através do DTO de Resposta (Prevenção de Vazamento de Dados)
        return new UserProfileDTO(
            usuarioSalvo.getUsername(),
            usuarioSalvo.getEmail(),
            usuarioSalvo.getXp(),
            usuarioSalvo.getLevel(),
            usuarioSalvo.getFitcoins()
        );
    }
}