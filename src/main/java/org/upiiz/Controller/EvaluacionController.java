package org.upiiz.Controller;

import org.upiiz.entities.Participante;
import org.upiiz.models.Evaluacion;
import org.upiiz.service.BienestarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/evaluacion")
@RequiredArgsConstructor
public class EvaluacionController {

    private final BienestarService bienestarService;

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        // CORRECCIÓN: Aseguramos que el objeto 'form' sea el que el HTML espera
        model.addAttribute("form", new Evaluacion());
        // Enviamos la constante de preguntas para que el th:each las renderice
        model.addAttribute("preguntas", BienestarService.PREGUNTAS);
        return "evaluacion";
    }

    @PostMapping("/guardar")
    public String guardarEvaluacion(
            @RequestParam String nombreCompleto,
            @RequestParam String sexo,
            @RequestParam Integer edad,
            @RequestParam String anioEscolar,
            @RequestParam String grupo,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes) {

        try {
            Map<Integer, Integer> respuestasMap = new HashMap<>();
            for (int i = 1; i <= 39; i++) {
                String key = "respuestas[" + i + "]";
                String val = allParams.get(key);
                if (val != null && !val.isBlank()) {
                    respuestasMap.put(i, Integer.parseInt(val));
                }
            }

            if (respuestasMap.size() < 39) {
                redirectAttributes.addFlashAttribute("error", "Por favor responde todas las preguntas.");
                return "redirect:/evaluacion/nueva";
            }

            Evaluacion form = new Evaluacion();
            form.setNombreCompleto(nombreCompleto);
            form.setSexo(sexo);
            form.setEdad(edad);
            form.setAnioEscolar(anioEscolar);
            form.setGrupo(grupo);
            form.setRespuestas(respuestasMap);

            Participante guardado = bienestarService.guardarEvaluacion(form);
            redirectAttributes.addFlashAttribute("exito", "Evaluación guardada.");
            return "redirect:/resultados/" + guardado.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/evaluacion/nueva";
        }
    }
}