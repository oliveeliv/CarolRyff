package org.upiiz.repository;

import org.upiiz.entities.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM Respuesta r WHERE r.participante.id = ?1")
    void deleteByParticipanteId(Long participanteId);
}