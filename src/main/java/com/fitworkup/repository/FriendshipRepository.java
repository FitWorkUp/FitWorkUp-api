package com.fitworkup.repository;

import com.fitworkup.models.Friendship;
import com.fitworkup.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Verifica se já existe um relacionamento (em qualquer direção)
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM Friendship f " +
           "WHERE (f.user = :user AND f.friend = :friend) " +
           "OR (f.user = :friend AND f.friend = :user)")
    boolean existsFriendshipRelation(@Param("user") User user, @Param("friend") User friend);

    // Busca um convite específico para poder aceitar/rejeitar
    Optional<Friendship> findByUserAndFriend(User user, User friend);

    // Lista os convites recebidos e pendentes de um usuário
    List<Friendship> findByFriendAndStatus(User friend, String status);

    // Lista todos os amigos (status = ACCEPTED) onde o usuário enviou ou recebeu o convite
    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND (f.user = :user OR f.friend = :user)")
    List<Friendship> findAllAcceptedFriendships(@Param("user") User user);
}