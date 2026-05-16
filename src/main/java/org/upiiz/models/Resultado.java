package org.upiiz.models;

import lombok.*;

/**
 * DTO para mostrar los resultados del participante
 * con los promedios por dimensión y el bienestar global.
 * Utiliza Lombok para reducir el código repetitivo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resultado {

    private Long participanteId;
    private String nombreCompleto;
    private String sexo;
    private Integer edad;
    private String anioEscolar;
    private String grupo;

    // Promedios por dimensión (escala 1-6)
    private Double autoaceptacion;
    private Double relacionesPositivas;
    private Double autonomia;
    private Double dominioEntorno;
    private Double propositoVida;
    private Double crecimientoPersonal;
    private Double bienestarGlobal;

    // --- MÉTODOS DE INTERPRETACIÓN (NIVELES) ---
    // Estos métodos permiten que Thymeleaf acceda a ellos como si fueran atributos.

    public String getNivelAutoaceptacion()      { return interpretarNivel(autoaceptacion); }
    public String getNivelRelacionesPositivas() { return interpretarNivel(relacionesPositivas); }
    public String getNivelAutonomia()           { return interpretarNivel(autonomia); }
    public String getNivelDominioEntorno()      { return interpretarNivel(dominioEntorno); }
    public String getNivelPropositoVida()       { return interpretarNivel(propositoVida); }
    public String getNivelCrecimientoPersonal() { return interpretarNivel(crecimientoPersonal); }
    public String getNivelBienestarGlobal()     { return interpretarNivel(bienestarGlobal); }

    private String interpretarNivel(Double valor) {
        if (valor == null) return "Sin datos";
        if (valor >= 5.0) return "Alto";
        if (valor >= 3.5) return "Medio";
        return "Bajo";
    }

    // --- MÉTODOS PARA UI (PORCENTAJES 0-100%) ---
    // Útiles para componentes visuales como barras de progreso en el HTML.

    public int getPorcentajeAutoaceptacion()      { return toPorcentaje(autoaceptacion); }
    public int getPorcentajeRelacionesPositivas() { return toPorcentaje(relacionesPositivas); }
    public int getPorcentajeAutonomia()           { return toPorcentaje(autonomia); }
    public int getPorcentajeDominioEntorno()      { return toPorcentaje(dominioEntorno); }
    public int getPorcentajePropositoVida()       { return toPorcentaje(propositoVida); }
    public int getPorcentajeCrecimientoPersonal() { return toPorcentaje(crecimientoPersonal); }
    public int getPorcentajeBienestarGlobal()     { return toPorcentaje(bienestarGlobal); }

    private int toPorcentaje(Double valor) {
        if (valor == null) return 0;
        // Convierte la escala de 1-6 a un porcentaje aproximado para la interfaz
        return (int) Math.round((valor / 6.0) * 100);
    }
}