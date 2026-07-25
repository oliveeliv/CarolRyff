package org.upiiz.repository;

import org.upiiz.entities.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    List<Participante> findByAnioEscolarAndGrupoOrderByNombreCompletoAsc(String anioEscolar, String grupo);
    List<Participante> findByGrupoOrderByNombreCompletoAsc(String grupo);
    List<Participante> findByAnioEscolarOrderByNombreCompletoAsc(String anioEscolar);

    @Query("SELECT DISTINCT p.anioEscolar FROM Participante p ORDER BY p.anioEscolar")
    List<String> findDistinctAnios();

    @Query("SELECT DISTINCT p.grupo FROM Participante p ORDER BY p.grupo")
    List<String> findDistinctGrupos();

    List<Participante> findAllByOrderByFechaAplicacionDesc();

    @Query("SELECT p.grupo, " +
            "AVG(p.promedioAutoaceptacion), " +
            "AVG(p.promedioRelacionesPositivas), " +
            "AVG(p.promedioAutonomia), " +
            "AVG(p.promedioDominioEntorno), " +
            "AVG(p.promedioPropositoVida), " +
            "AVG(p.promedioCrecimientoPersonal) " +
            "FROM Participante p " +
            "GROUP BY p.grupo " +
            "ORDER BY p.grupo ASC")
    List<Object[]> findPromediosPorGrupo();

    @Query("SELECT COUNT(p) FROM Participante p WHERE p.nombreCompleto = ?1 AND p.grupo = ?2 AND p.anioEscolar = ?3 AND p.fechaAplicacion > ?4")
    long contarDuplicadosRecientes(String nombre, String grupo, String anio, LocalDateTime desde);

    Optional<Participante> findTopByNombreCompletoAndGrupoOrderByFechaAplicacionDesc(
            String nombreCompleto, String grupo);
}