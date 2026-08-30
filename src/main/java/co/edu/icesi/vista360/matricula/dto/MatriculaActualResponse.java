package co.edu.icesi.vista360.matricula.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Matricula del periodo academico vigente.
 *
 * <p>Devuelve el conjunto completo del periodo, canceladas incluidas, y expone el estado de
 * cada inscripcion. El razonamiento esta en el S-14 de docs/supuestos.md.
 */
@Schema(name = "MatriculaActual", description = """
        Materias del periodo académico vigente con el estado y la nota de cada una.
        Incluye siempre las inscritas y las canceladas (S-14).""")
public record MatriculaActualResponse(

        @Schema(description = "Código institucional del estudiante (S-11)",
                example = "A00123456", requiredMode = RequiredMode.REQUIRED)
        String estudianteId,

        @Schema(description = "Periodo académico al que corresponde la matrícula",
                requiredMode = RequiredMode.REQUIRED)
        Periodo periodo,

        @Schema(description = """
                Programas en los que el estudiante está matriculado este periodo. Puede traer
                uno sin inscripciones propias, así que no se deriva de la lista de materias.""",
                requiredMode = RequiredMode.REQUIRED)
        List<ProgramaMatriculado> programas,

        @Schema(description = """
                Instante de la última sincronización. Lo académico es una réplica local
                alimentada por sondeo, no una lectura en vivo del ERP (S-01, S-02).""",
                example = "2026-08-30T04:15:00Z", requiredMode = RequiredMode.REQUIRED)
        Instant actualizadoEn,

        @Schema(description = """
                Inscripciones del periodo. Vacía si el estudiante está matriculado y todavía
                no inscribe materias.""",
                requiredMode = RequiredMode.REQUIRED)
        List<Materia> materias) {

    @Schema(name = "PeriodoAcademico", description = "Periodo académico vigente")
    public record Periodo(

            @Schema(description = "Año seguido del número de periodo", example = "202620",
                    requiredMode = RequiredMode.REQUIRED)
            String codigo,

            @Schema(example = "2026-07-27", requiredMode = RequiredMode.REQUIRED)
            LocalDate fechaInicio,

            @Schema(example = "2026-11-28", requiredMode = RequiredMode.REQUIRED)
            LocalDate fechaFin) {
    }

    @Schema(name = "Programa",
            description = "Programa académico bajo el cual se inscribió la asignatura")
    public record Programa(

            @Schema(example = "TEL", requiredMode = RequiredMode.REQUIRED) String codigo,

            @Schema(example = "Ingeniería Telemática", requiredMode = RequiredMode.REQUIRED)
            String nombre) {
    }

    @Schema(name = "ProgramaMatriculado", description = """
            Programa de la matrícula del periodo. El principal es sobre el que el ERP calcula
            semestre y promedio, y el que se asume cuando una inscripción no declara programa.""")
    public record ProgramaMatriculado(

            @Schema(example = "TEL", requiredMode = RequiredMode.REQUIRED) String codigo,

            @Schema(example = "Ingeniería Telemática", requiredMode = RequiredMode.REQUIRED)
            String nombre,

            @Schema(description = "Verdadero en exactamente un programa de la matrícula",
                    example = "true", requiredMode = RequiredMode.REQUIRED)
            boolean principal) {
    }

    @Schema(name = "Asignatura", description = """
            Datos de la asignatura en el catálogo. No dependen de quién la inscribió: una
            misma asignatura la cursan estudiantes de programas distintos.""")
    public record Asignatura(

            @Schema(description = "Código de la asignatura, cinco dígitos", example = "09780",
                    requiredMode = RequiredMode.REQUIRED)
            String codigo,

            @Schema(example = "Ciberseguridad", requiredMode = RequiredMode.REQUIRED)
            String nombre,

            @Schema(description = "Créditos académicos", example = "3",
                    requiredMode = RequiredMode.REQUIRED)
            int creditos) {
    }

    @Schema(name = "MateriaMatriculada", description = """
            El programa, el NRC, el grupo y los estados
            son atributos de la inscripción y no del catálogo (S-09, S-14, S-15).""")
    public record Materia(

            @Schema(description = "Asignatura del catálogo que se inscribió",
                    requiredMode = RequiredMode.REQUIRED)
            Asignatura asignatura,

            @Schema(description = """
                    Identifica el grupo de forma única dentro del periodo. Ante discrepancia
                    con el grupo, manda el NRC.""",
                    example = "11008", requiredMode = RequiredMode.REQUIRED)
            String nrc,

            @Schema(description = """
                    Número de grupo que el estudiante ve en su horario. Es etiqueta de
                    presentación y no identifica por sí solo.""",
                    example = "001", requiredMode = RequiredMode.REQUIRED)
            String grupo,

            @Schema(description = """
                    Programa bajo el cual el ERP registró esta inscripción. Que la asignatura
                    cuente además para otro programa es homologación, fuera de alcance (S-15).""",
                    requiredMode = RequiredMode.REQUIRED)
            Programa programa,

            @Schema(example = "Ana María Restrepo", requiredMode = RequiredMode.REQUIRED)
            String docente,

            @Schema(description = """
                    Si la inscripción sigue vigente o fue cancelada. Es un eje independiente
                    del estado de calificación (S-14).""",
                    requiredMode = RequiredMode.REQUIRED)
            EstadoInscripcion estadoInscripcion,

            @Schema(description = """
                    Con qué se califica la asignatura. Se conoce al inscribir, antes de que
                    exista calificación, y decide si viene nota o resultado (S-16).""",
                    requiredMode = RequiredMode.REQUIRED)
            EscalaCalificacion escalaCalificacion,

            @Schema(description = """
                    Hasta dónde llegó la calificación. No se deriva de la nota ni del estado
                    de inscripción (S-14).""",
                    requiredMode = RequiredMode.REQUIRED)
            EstadoCalificacion estadoCalificacion,

            @Schema(description = """
                    Nota en escala 0.0 a 5.0, solo con escala NUMERICA. Nula si el estado de
                    calificación es PENDIENTE.""",
                    example = "4.2")
            BigDecimal nota,

            @Schema(description = """
                    Calificación cualitativa, solo con escala APROBACION. Nula si el estado de
                    calificación es PENDIENTE.""",
                    example = "APROBADA")
            ResultadoAprobacion resultado,

            @Schema(description = """
                    Fecha en que se canceló la inscripción. No nula si y solo si el estado de
                    inscripción es CANCELADA.""",
                    example = "2026-08-21")
            LocalDate fechaCancelacion) {
    }

    @Schema(name = "EstadoInscripcion", description = """
            INSCRITA: la inscripción sigue vigente.
            CANCELADA: el estudiante canceló la materia durante el periodo.""")
    public enum EstadoInscripcion {
        INSCRITA,
        CANCELADA
    }

    @Schema(name = "EscalaCalificacion", description = """
            NUMERICA: se califica con nota de 0.0 a 5.0.
            APROBACION: se califica como aprobada o reprobada, sin nota.""")
    public enum EscalaCalificacion {
        NUMERICA,
        APROBACION
    }

    @Schema(name = "EstadoCalificacion", description = """
            PENDIENTE: todavía no hay calificación cargada.
            PARCIAL: hay nota acumulada y el periodo no ha cerrado para esa materia.
            DEFINITIVA: la calificación es la del cierre y no cambia.""")
    public enum EstadoCalificacion {
        PENDIENTE,
        PARCIAL,
        DEFINITIVA
    }

    @Schema(name = "ResultadoAprobacion", description = """
            APROBADA o REPROBADA. Solo aparece con escala APROBACION, que no admite estado
            de calificación PARCIAL porque un aprobado parcial no significa nada.""")
    public enum ResultadoAprobacion {
        APROBADA,
        REPROBADA
    }
}
