package org.upiiz.models;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evaluacion {

    // DATOS SOCIODEMOGRÁFICOS

    @NotBlank(message = "El nombre completo es requerido")
    private String nombreCompleto;

    @NotBlank(message = "El sexo es requerido")
    private String sexo;

    @NotNull(message = "La edad es requerida")
    @Min(value = 1) @Max(value = 120)
    private Integer edad;

    @NotBlank(message = "El año escolar es requerido")
    private String anioEscolar;

    @NotBlank(message = "El grupo es requerido")
    private String grupo;

    private Map<Integer, Integer> respuestas = new HashMap<>();

    public Integer getRespuesta(int numero) {
        return respuestas.get(numero);
    }

    public void setRespuesta(int numero, int valor) {
        respuestas.put(numero, valor);
    }
}