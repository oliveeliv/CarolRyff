package org.upiiz.service;

import org.upiiz.entities.Participante;
import org.upiiz.entities.Respuesta;
import org.upiiz.models.Resultado;
import org.upiiz.repository.ParticipanteRepository;
import org.upiiz.repository.RespuestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final RespuestaRepository respuestaRepository;
    private final BienestarService bienestarService;

    public List<Participante> listarTodos() {
        return participanteRepository.findAllByOrderByFechaAplicacionDesc();
    }

    public List<Resultado> listarTodosComoDTO() {
        return listarTodos().stream()
                .map(bienestarService::toResultado)
                .toList();
    }

    public Optional<Participante> buscarPorId(Long id) {
        return participanteRepository.findById(id);
    }

    public Optional<Resultado> buscarResultadoPorId(Long id) {
        return participanteRepository.findById(id)
                .map(bienestarService::toResultado);
    }

    public List<Resultado> filtrarResultados(String grupo, String anio) {
        return filtrarPorGrupoYAnio(grupo, anio)
                .stream()
                .map(bienestarService::toResultado)
                .toList();
    }

    public List<Participante> filtrarPorGrupoYAnio(String grupo, String anio) {
        if (grupo != null && !grupo.isBlank() && anio != null && !anio.isBlank()) {
            return participanteRepository
                    .findByAnioEscolarAndGrupoOrderByNombreCompletoAsc(anio, grupo);
        } else if (grupo != null && !grupo.isBlank()) {
            return participanteRepository
                    .findByGrupoOrderByNombreCompletoAsc(grupo);
        } else if (anio != null && !anio.isBlank()) {
            return participanteRepository
                    .findByAnioEscolarOrderByNombreCompletoAsc(anio);
        }
        return listarTodos();
    }

    @Transactional
    public void eliminar(Long id) {
        respuestaRepository.deleteByParticipanteId(id);
        participanteRepository.deleteById(id);
    }

    public List<String> obtenerAnios() {
        return participanteRepository.findDistinctAnios();
    }

    public List<String> obtenerGrupos() {
        return participanteRepository.findDistinctGrupos();
    }

    public List<Object[]> obtenerPromediosGrupales() {
        return participanteRepository.findPromediosPorGrupo();
    }

    public List<Respuesta> obtenerRespuestasPorParticipante(Long id) {
        return respuestaRepository.findByParticipanteIdOrderByNumeroPreguntaAsc(id);
    }
}