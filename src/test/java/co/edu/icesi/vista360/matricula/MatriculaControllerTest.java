package co.edu.icesi.vista360.matricula;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.icesi.vista360.config.SeguridadConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de contrato del endpoint. Fijan la forma de la respuesta.
 */
@WebMvcTest(MatriculaController.class)
@Import(SeguridadConfig.class)
class MatriculaControllerTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    /** La inscripcion cancelada trae nota parcial y fecha: el estudiante cancelo perdiendo. */
    private static final String CANCELADA = "$.materias[?(@.asignatura.codigo=='09794')]";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devuelveLaMatriculaDelPeriodoConLaCanceladaIncluida() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value("A00123456"))
                .andExpect(jsonPath("$.periodo.codigo").value("202620"))
                .andExpect(jsonPath("$.actualizadoEn").value("2026-08-30T04:15:00Z"))
                .andExpect(jsonPath("$.materias", hasSize(5)))
                .andExpect(jsonPath(CANCELADA + ".estadoInscripcion").value("CANCELADA"))
                .andExpect(jsonPath(CANCELADA + ".estadoCalificacion").value("PARCIAL"))
                .andExpect(jsonPath(CANCELADA + ".nota").value(2.1))
                .andExpect(jsonPath(CANCELADA + ".fechaCancelacion").value("2026-08-21"))
                .andExpect(jsonPath(CANCELADA + ".nrc").value("11384"))
                .andExpect(jsonPath(CANCELADA + ".grupo").value("001"));
    }

    /** PENDIENTE y nota nula van siempre juntos. */
    @Test
    void laMateriaSinCalificarNoTraeNota() throws Exception {
        String plataformas = "$.materias[?(@.asignatura.codigo=='09791')]";

        mockMvc.perform(get(RUTA, "A00123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(plataformas + ".estadoCalificacion").value("PENDIENTE"))
                .andExpect(jsonPath(plataformas + ".nota").value(contains(nullValue())))
                .andExpect(jsonPath(plataformas + ".fechaCancelacion").value(contains(nullValue())));
    }

    /** El programa cuelga de la inscripcion: la doble titulacion cabe en un solo listado. */
    @Test
    void consolidaLosDosProgramasEnUnSoloListado() throws Exception {
        mockMvc.perform(get(RUTA, "A00987654"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias[*].programa.codigo")
                        .value(containsInAnyOrder("TEL", "TEL", "SIS", "SIS")));
    }

    @Test
    void rechazaElCodigoQueNoCumpleElFormato() throws Exception {
        mockMvc.perform(get(RUTA, "XYZ"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void respondeNotFoundCuandoElCodigoNoCorrespondeAUnEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
