package com.fitworkup.repository;

import com.fitworkup.models.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);


    // SEGURANÇA & ARQUITETURA: Ranking dinâmico calculado por XP. 
    // Elimina a necessidade de tabelas temporárias ou de controle para o Placar de Líderes.
    List<User> findTop10ByOrderByXpDesc();
}