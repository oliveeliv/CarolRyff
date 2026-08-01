package org.upiiz.controller;

import org.upiiz.entities.Participante;
import org.upiiz.models.Evaluacion;
import org.upiiz.service.BienestarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/evaluacion")
@RequiredArgsConstructor
public class EvaluacionController {

    private final BienestarService bienestarService;

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new Evaluacion());
        }
        model.addAttribute("preguntas", BienestarService.PREGUNTAS);
        return "evaluacion";
    }

    @PostMapping("/guardar")
    public String guardarEvaluacion(
            @ModelAttribute("form") Evaluacion form,
            RedirectAttributes redirectAttributes) {

        try {
            // Validar que el map de respuestas no venga nulo y tenga las 39 preguntas
            if (form.getRespuestas() == null || form.getRespuestas().size() < 39) {
                redirectAttributes.addFlashAttribute("error", "Por favor responde todas las preguntas del cuestionario.");
                redirectAttributes.addFlashAttribute("form", form);
                return "redirect:/evaluacion/nueva";
            }

            Participante guardado = bienestarService.guardarEvaluacion(form);
            redirectAttributes.addFlashAttribute("exito", "Evaluación guardada correctamente.");
            return "redirect:/resultados/" + guardado.getId();

        } catch (Exception e) {
            // Captura cualquier error de servicio o de BD evitando la pantalla de HTTP 500
            redirectAttributes.addFlashAttribute("error", "Error al procesar la evaluación: " + e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/evaluacion/nueva";
        }
    }
}