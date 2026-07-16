package com.fitworkup.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitworkup.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Busca usuário pelo Nick único (para a tela de busca)
    Optional<User> findByUsername(String username);
    
    // Busca usuário pelo e-mail (usado no login tradicional)
    Optional<User> findByEmail(String email);
    
    // Busca usuário pelo ID do Google (usado no Login Social)
    Optional<User> findByGoogleId(String googleId);
}