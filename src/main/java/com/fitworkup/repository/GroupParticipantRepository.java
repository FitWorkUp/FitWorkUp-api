package com.fitworkup.repository;

import com.fitworkup.models.GroupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupParticipantRepository extends JpaRepository<GroupParticipant, Long> {
    Optional<GroupParticipant> findByGroupSessionIdAndUserId(Long groupSessionId, Long userId);
    boolean existsByGroupSessionIdAndUserId(Long groupSessionId, Long userId);
    long countByGroupSessionId(Long groupSessionId);

    @Query("SELECT gp FROM GroupParticipant gp JOIN FETCH gp.user " +
           "WHERE gp.groupSession.id = :sessionId ORDER BY gp.joinedAt ASC")
    List<GroupParticipant> findAllWithUserBySessionId(@Param("sessionId") Long sessionId);
}
