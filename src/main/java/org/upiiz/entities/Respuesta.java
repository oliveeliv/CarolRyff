package org.upiiz.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "respuestas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @Column(name = "numero_pregunta", nullable = false)
    private Integer numeroPregunta;

    /**
     * Valor tal como lo marcó el participante (1-6)
     */
    @Column(name = "valor_original", nullable = false)
    private Integer valorOriginal;

    /**
     * Valor después de invertir (si la pregunta es inversa)
     * Fórmula inversa: 7 - valorOriginal
     */
    @Column(name = "valor_procesado", nullable = false)
    private Integer valorProcesado;

    /**
     * Dimensión a la que pertenece la pregunta:
     * AUTOACEPTACION, RELACIONES_POSITIVAS, AUTONOMIA,
     * DOMINIO_ENTORNO, PROPOSITO_VIDA, CRECIMIENTO_PERSONAL
     */
    @Column(name = "dimension", nullable = false, length = 50)
    private String dimension;
}