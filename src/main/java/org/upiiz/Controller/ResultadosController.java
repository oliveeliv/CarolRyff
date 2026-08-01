package org.upiiz.controller;

import org.upiiz.entities.Participante;
import org.upiiz.models.Resultado;
import org.upiiz.service.ExcelService;
import org.upiiz.service.ParticipanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final ExcelService excelService;

    // 1. LISTA GENERAL
    @GetMapping
    public String listarResultados(
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) String anio,
            Model model) {
        try {
            List<Resultado> resultados = participanteService.filtrarResultados(grupo, anio);
            model.addAttribute("resultados", resultados != null ? resultados : new ArrayList<>());
            model.addAttribute("grupos", participanteService.obtenerGrupos());
            model.addAttribute("anios", participanteService.obtenerAnios());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("resultados", new ArrayList<>());
            model.addAttribute("grupos", List.of());
            model.addAttribute("anios", List.of());
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        model.addAttribute("filtroAnio", anio != null ? anio : "");
        model.addAttribute("filtroGrupo", grupo != null ? grupo : "");
        return "lista";
    }

    // 2. EXPORTAR A EXCEL (Ruta específica)
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false, defaultValue = "") String grupo,
            @RequestParam(required = false, defaultValue = "") String anio) {
        try {
            String g = (grupo != null && !grupo.isBlank()) ? grupo : null;
            String a = (anio != null && !anio.isBlank()) ? anio : null;

            List<Resultado> resultados = participanteService.filtrarResultados(g, a);
            if (resultados == null) {
                resultados = new ArrayList<>();
            }

            byte[] excelBytes = excelService.generarExcel(resultados);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bienestar_ryff.xlsx\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            System.err.println("❌ EXCEPCIÓN AL EXPORTAR EXCEL:");
            e.printStackTrace(); // Imprime el error exacto en Render
            return ResponseEntity.internalServerError().build();
        }
    }

    // 3. GRÁFICA GRUPAL (Ruta específica)
    @GetMapping("/comparativa-grupal")
    public String mostrarGraficaGrupal(Model model) {
        try {
            List<Participante> todos = participanteService.filtrarPorGrupoYAnio(null, null);
            if (todos == null) todos = new ArrayList<>();

            Map<String, List<Participante>> gruposMap = todos.stream()
                    .filter(p -> p != null && p.getGrupo() != null)
                    .collect(Collectors.groupingBy(Participante::getGrupo));

            List<Object[]> promediosGrupos = new ArrayList<>();
            gruposMap.forEach((nombreGrupo, participantes) -> {
                promediosGrupos.add(new Object[]{
                        nombreGrupo,
                        avg(participantes, "autoaceptacion"),
                        avg(participantes, "relacionesPositivas"),
                        avg(participantes, "autonomia"),
                        avg(participantes, "dominioEntorno"),
                        avg(participantes, "propositoVida"),
                        avg(participantes, "crecimientoPersonal")
                });
            });
            model.addAttribute("promediosGrupos", promediosGrupos);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("promediosGrupos", new ArrayList<>());
            model.addAttribute("error", "Error al cargar gráfica: " + e.getMessage());
        }
        return "grafica_grupal";
    }

    // 4. PROMEDIO POR GRUPO (Ruta específica REST API)
    @GetMapping("/grupo-promedio")
    @ResponseBody
    public Map<String, Double> promedioPorGrupo(@RequestParam String grupo) {
        try {
            List<Participante> participantes = participanteService
                    .filtrarPorGrupoYAnio(grupo, null);
            if (participantes == null || participantes.isEmpty()) return datosVacios();

            Map<String, Double> promedios = new HashMap<>();
            promedios.put("autoaceptacion",      avg(participantes, "autoaceptacion"));
            promedios.put("relacionesPositivas",  avg(participantes, "relacionesPositivas"));
            promedios.put("autonomia",            avg(participantes, "autonomia"));
            promedios.put("dominioEntorno",       avg(participantes, "dominioEntorno"));
            promedios.put("propositoVida",        avg(participantes, "propositoVida"));
            promedios.put("crecimientoPersonal",  avg(participantes, "crecimientoPersonal"));
            return promedios;
        } catch (Exception e) {
            e.printStackTrace();
            return datosVacios();
        }
    }

    // 5. DETALLE INDIVIDUAL
    @GetMapping("/{id}")
    public String verDetalleIndividual(@PathVariable Long id, Model model) {
        try {
            Optional<Resultado> resultado = participanteService.buscarResultadoPorId(id);
            if (resultado.isEmpty()) return "redirect:/resultados";
            model.addAttribute("resultado", resultado.get());
            model.addAttribute("grupos", participanteService.obtenerGrupos());
            return "resultados";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/resultados";
        }
    }

    // 6. ELIMINAR
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            participanteService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Registro eliminado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/resultados";
    }

    // HELPERS
    private double avg(List<Participante> lista, String dim) {
        if (lista == null || lista.isEmpty()) return 0;
        return lista.stream().mapToDouble(p -> switch (dim) {
            case "autoaceptacion"      -> p.getPromedioAutoaceptacion()      != null ? p.getPromedioAutoaceptacion()      : 0;
            case "relacionesPositivas" -> p.getPromedioRelacionesPositivas() != null ? p.getPromedioRelacionesPositivas() : 0;
            case "autonomia"           -> p.getPromedioAutonomia()           != null ? p.getPromedioAutonomia()           : 0;
            case "dominioEntorno"      -> p.getPromedioDominioEntorno()      != null ? p.getPromedioDominioEntorno()      : 0;
            case "propositoVida"       -> p.getPromedioPropositoVida()       != null ? p.getPromedioPropositoVida()       : 0;
            case "crecimientoPersonal" -> p.getPromedioCrecimientoPersonal() != null ? p.getPromedioCrecimientoPersonal() : 0;
            default -> 0;
        }).average().orElse(0);
    }

    private Map<String, Double> datosVacios() {
        Map<String, Double> m = new HashMap<>();
        m.put("autoaceptacion", 0.0);
        m.put("relacionesPositivas", 0.0);
        m.put("autonomia", 0.0);
        m.put("dominioEntorno", 0.0);
        m.put("propositoVida", 0.0);
        m.put("crecimientoPersonal", 0.0);
        return m;
    }
}