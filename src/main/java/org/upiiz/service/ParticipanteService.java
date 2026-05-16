package org.upiiz.service;

import org.upiiz.entities.Participante;
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

    // LISTAR TODOS
    public List<Participante> listarTodos() {
        return participanteRepository.findAllByOrderByFechaAplicacionDesc();
    }

    public List<Resultado> listarTodosComoDTO() {
        return listarTodos().stream()
                .map(bienestarService::toResultado)
                .toList();
    }

    // BUSCAR POR ID
    public Optional<Participante> buscarPorId(Long id) {
        return participanteRepository.findById(id);
    }

    public Optional<Resultado> buscarResultadoPorId(Long id) {
        return participanteRepository.findById(id)
                .map(bienestarService::toResultado);
    }

    // FILTRAR — devuelve List<Resultado> (usado en lista general)
    public List<Resultado> filtrarResultados(String grupo, String anio) {
        return filtrarPorGrupoYAnio(grupo, anio)
                .stream()
                .map(bienestarService::toResultado)
                .toList();
    }

    // FILTRAR — devuelve List<Participante> (usado en promedio por grupo)
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

    // ELIMINAR
    @Transactional
    public void eliminar(Long id) {
        respuestaRepository.deleteByParticipanteId(id);
        participanteRepository.deleteById(id);
    }

    // CATÁLOGOS
    public List<String> obtenerAnios() {
        return participanteRepository.findDistinctAnios();
    }

    public List<String> obtenerGrupos() {
        return participanteRepository.findDistinctGrupos();
    }

    public List<Object[]> obtenerPromediosGrupales() {
        return participanteRepository.findPromediosPorGrupo();
    }
}