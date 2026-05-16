package org.upiiz.Controller;

import org.upiiz.entities.Participante;
import org.upiiz.models.Resultado;
import org.upiiz.service.ParticipanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/resultados")
@RequiredArgsConstructor
public class ResultadosController {

    private final ParticipanteService participanteService;

    @GetMapping
    public String listarResultados(
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String anio,
            Model model) {
        try {
            List<Resultado> resultados = new ArrayList<>();
            for (Participante p : participanteService.filtrarPorGrupoYAnio(grupo, anio)) {
                participanteService.buscarResultadoPorId(p.getId())
                        .ifPresent(resultados::add);
            }
            model.addAttribute("resultados", resultados);
            model.addAttribute("grupos", participanteService.obtenerGrupos());
            model.addAttribute("anios", participanteService.obtenerAnios());
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        model.addAttribute("filtroAnio", anio);
        model.addAttribute("filtroGrupo", grupo);
        return "lista";
    }

    @GetMapping("/comparativa-grupal")
    public String mostrarGraficaGrupal(Model model) {
        try {
            List<Participante> todos = participanteService.filtrarPorGrupoYAnio(null, null);
            Map<String, List<Participante>> gruposMap = todos.stream()
                    .filter(p -> p.getGrupo() != null)
                    .collect(Collectors.groupingBy(Participante::getGrupo));

            List<Object[]> promediosGrupos = new ArrayList<>();
            gruposMap.forEach((nombreGrupo, participantes) -> {
                double avgAuto = avg(participantes, "autoaceptacion");
                double avgRela = avg(participantes, "relacionesPositivas");
                double avgAutoNo = avg(participantes, "autonomia");
                double avgDom = avg(participantes, "dominioEntorno");
                double avgProp = avg(participantes, "propositoVida");
                double avgCrec = avg(participantes, "crecimientoPersonal");
                promediosGrupos.add(new Object[]{nombreGrupo, avgAuto, avgRela, avgAutoNo, avgDom, avgProp, avgCrec});
            });
            model.addAttribute("promediosGrupos", promediosGrupos);
        } catch (Exception e) {
            model.addAttribute("promediosGrupos", new ArrayList<>());
        }
        // CORRECCIÓN: Apunta al nombre real de tu archivo físico
        return "grafica_grupal";
    }

    @GetMapping("/{id}")
    public String verDetalleIndividual(@PathVariable Long id, Model model) {
        try {
            Optional<Resultado> resultado = participanteService.buscarResultadoPorId(id);
            if (resultado.isEmpty()) return "redirect:/resultados";
            model.addAttribute("resultado", resultado.get());
            return "resultados";
        } catch (Exception e) {
            return "redirect:/resultados";
        }
    }

    private double avg(List<Participante> lista, String dim) {
        return lista.stream().mapToDouble(p -> {
            return switch (dim) {
                case "autoaceptacion"     -> p.getPromedioAutoaceptacion() != null ? p.getPromedioAutoaceptacion() : 0;
                case "relacionesPositivas"-> p.getPromedioRelacionesPositivas() != null ? p.getPromedioRelacionesPositivas() : 0;
                case "autonomia"          -> p.getPromedioAutonomia() != null ? p.getPromedioAutonomia() : 0;
                case "dominioEntorno"     -> p.getPromedioDominioEntorno() != null ? p.getPromedioDominioEntorno() : 0;
                case "propositoVida"      -> p.getPromedioPropositoVida() != null ? p.getPromedioPropositoVida() : 0;
                case "crecimientoPersonal"-> p.getPromedioCrecimientoPersonal() != null ? p.getPromedioCrecimientoPersonal() : 0;
                default -> 0;
            };
        }).average().orElse(0);
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        participanteService.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Registro eliminado.");
        return "redirect:/resultados";
    }
}