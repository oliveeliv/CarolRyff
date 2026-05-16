package org.upiiz.repository;

import org.upiiz.entities.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

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

    // NUEVA CONSULTA: Promedios por grupo
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
}