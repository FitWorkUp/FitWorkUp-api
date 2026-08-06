package com.fitworkup.repository;

import com.fitworkup.models.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUserIdAndStatus(Long userId, String status);

    List<Friendship> findByFriendIdAndStatus(Long friendId, String status);

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    // Checa vínculo em ambas as direções
    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :u1 AND f.friend.id = :u2) OR (f.user.id = :u2 AND f.friend.id = :u1)")
    Optional<Friendship> findFriendshipBetween(@Param("u1") Long u1, @Param("u2") Long u2);

    // Retorna todos os amigos confirmados de um usuário
    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriendships(@Param("userId") Long userId);
}