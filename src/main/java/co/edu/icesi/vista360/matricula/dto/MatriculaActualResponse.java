package co.edu.icesi.vista360.matricula.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta de la matricula del periodo vigente. temporal hasta modelado de datos
 */
@Schema(name = "MatriculaActual", description = "Materias del periodo vigente con su nota")
public record MatriculaActualResponse(

        @Schema(description = "Codigo institucional del estudiante", example = "A00123456")
        String estudianteId,

        @Schema(description = "Periodo academico al que corresponde la matricula")
        Periodo periodo,

        @Schema(description = "Materias inscritas. Vacia si esta matriculado pero aun no inscribe")
        List<Materia> materias) {

    @Schema(name = "PeriodoAcademico")
    public record Periodo(

            @Schema(description = "Codigo del periodo", example = "2026-02")
            String codigo,

            @Schema(example = "2026-07-27") LocalDate fechaInicio,

            @Schema(example = "2026-11-28") LocalDate fechaFin) {
    }

    @Schema(name = "MateriaMatriculada")
    public record Materia(

            @Schema(description = "Codigo de la asignatura", example = "TEL301")
            String codigo,

            @Schema(example = "Arquitectura de Software") String nombre,

            @Schema(description = "Creditos academicos", example = "3") int creditos,

            @Schema(description = "Grupo o NRC", example = "01") String grupo,

            @Schema(example = "Ana Maria Restrepo") String docente,

            @Schema(description = "Nota en escala 0.0 a 5.0. Null si aun no hay calificacion",
                    example = "4.2")
            BigDecimal notaDefinitiva) {
    }
}
