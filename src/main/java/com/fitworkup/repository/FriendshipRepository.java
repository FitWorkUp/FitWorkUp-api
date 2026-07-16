package com.fitworkup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitworkup.models.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // Busca todas as amizades aceitas ou pendentes de um usuário específico
    List<Friendship> findByUserIdAndStatus(Long userId, String status);
    
    // Busca todas as solicitações recebidas por um usuário (para a tela de convites)
    List<Friendship> findByFriendIdAndStatus(Long friendId, String status);
    
    // Verifica se já existe um vínculo (pendente ou aceito) entre dois usuários
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
}