package org.upiiz.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.upiiz.service.ParticipanteService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ParticipanteService participanteService;

    @GetMapping("/")
    public String index(Model model) {
        // model.addAttribute("totalParticipantes", participanteService.listarTodos().size());
        model.addAttribute("totalParticipantes", 0); // Valor temporal para evitar el error 500
        return "index";
    }
}

