package co.edu.icesi.vista360.matricula;

import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Materia;
import co.edu.icesi.vista360.matricula.dto.MatriculaActualResponse.Periodo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Slice vertical: datos quemados, repositorio, servicio, entidades.
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
public class MatriculaController {

    private static final Periodo PERIODO_VIGENTE =
            new Periodo("2026-02", LocalDate.of(2026, 7, 27), LocalDate.of(2026, 11, 28));
        
    private static final Map<String, List<Materia>> MATERIAS_POR_ESTUDIANTE = Map.of(
            "A00123456", List.of(
                    new Materia("TEL301", "Arquitectura de Software", 3, "01",
                            "Ana Maria Restrepo", new BigDecimal("4.2")),
                    new Materia("TEL310", "Redes de Computadores II", 4, "02",
                            "Carlos Andres Zapata", new BigDecimal("3.6")),
                    new Materia("TEL325", "Computacion en la Nube", 3, "01",
                            "Diana Lucia Ospina", new BigDecimal("4.5")),
                    // Sin calificar todavia: el caso que obliga a que el campo sea nullable.
                    new Materia("HUM210", "Etica y Responsabilidad Social", 2, "07",
                            "Jorge Enrique Valencia", null)),
            "A00987654", List.of(
                    new Materia("TEL208", "Bases de Datos", 4, "03",
                            "Ana Maria Restrepo", new BigDecimal("3.1")),
                    new Materia("TEL215", "Sistemas Operativos", 3, "01",
                            "Felipe Andres Marin", new BigDecimal("2.8")),
                    new Materia("MAT180", "Estadistica Aplicada", 3, "05",
                            "Claudia Patricia Nieto", null)));

    @Operation(
            summary = "Materias del periodo vigente con su nota",
            description = """
                    Dado el codigo institucional del estudiante, devuelve las materias
                    del periodo academico vigente y la nota de cada una.

                    Un estudiante matriculado que aun no ha inscrito materias responde
                    200 con la lista vacia. El 404 se reserva para el codigo
                    que no corresponde a ningun estudiante.

                    Datos quemados: solo A00123456 y A00987654 devuelven contenido.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matricula encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe un estudiante con ese codigo",
                    content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping("/{estudianteId}/matricula-actual")
    public MatriculaActualResponse matriculaActual(
            @Parameter(description = "Codigo institucional", example = "A00123456")
            @PathVariable String estudianteId) {

        List<Materia> materias = MATERIAS_POR_ESTUDIANTE.get(estudianteId);
        if (materias == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un estudiante con codigo " + estudianteId);
        }
        return new MatriculaActualResponse(estudianteId, PERIODO_VIGENTE, materias);
    }
}
