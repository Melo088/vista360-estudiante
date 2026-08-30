package co.edu.icesi.vista360.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Cada consulta deja rastro, incluida la que se rechaza (Escenario 4B). */
@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaAccesoTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaAccesoRepository auditoria;

    /** La auditoria escribe en transaccion propia, asi que la limpieza tambien se confirma. */
    @BeforeEach
    void limpiar() {
        auditoria.deleteAll();
    }

    private static RequestPostProcessor comoEstudiante(String sujeto) {
        return jwt().jwt(token -> token.subject(sujeto).claim("rol", "ESTUDIANTE"));
    }

    private AuditoriaAcceso unicaFila() {
        List<AuditoriaAcceso> filas = auditoria.findAll();
        assertEquals(1, filas.size(), "se esperaba exactamente una fila de auditoria");
        return filas.getFirst();
    }

    @Test
    void elAccesoPermitidoQuedaRegistradoConQuienQueYCuando() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456"))
                        .header("User-Agent", "curl/8.5.0"))
                .andExpect(status().isOk());

        AuditoriaAcceso fila = unicaFila();
        assertEquals("A00123456", fila.getSujetoId());
        assertEquals("ESTUDIANTE", fila.getRol());
        assertEquals("A00123456", fila.getEstudianteConsultado());
        assertEquals("PERMITIDO", fila.getResultado());
        assertEquals("/api/v1/estudiantes/A00123456/matricula-actual", fila.getRecurso());
        assertNotNull(fila.getOcurridoEn());
        assertNotNull(fila.getDireccionIp());
        assertNotNull(fila.getCorrelacionId());
    }

    /** El acceso denegado es el que hay que poder demostrar despues del reclamo. */
    @Test
    void elAccesoDenegadoTambienQuedaRegistrado() throws Exception {
        mockMvc.perform(get(RUTA, "A00987654").with(comoEstudiante("A00123456")))
                .andExpect(status().isForbidden());

        AuditoriaAcceso fila = unicaFila();
        assertEquals("A00123456", fila.getSujetoId());
        assertEquals("A00987654", fila.getEstudianteConsultado());
        assertEquals("DENEGADO", fila.getResultado());
    }

    @Test
    void laPeticionSinTokenQuedaComoAnonimaYDenegada() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456")).andExpect(status().isUnauthorized());

        AuditoriaAcceso fila = unicaFila();
        assertEquals(AuditoriaAcceso.SUJETO_ANONIMO, fila.getSujetoId());
        assertEquals(AuditoriaAcceso.ROL_NINGUNO, fila.getRol());
        assertEquals("DENEGADO", fila.getResultado());
    }

    /** El permiso se concedio aunque no hubiera nada que entregar. */
    @Test
    void elAccesoAutorizadoAUnEstudianteInexistenteQuedaComoPermitido() throws Exception {
        mockMvc.perform(get(RUTA, "A00999999").with(comoEstudiante("A00999999")))
                .andExpect(status().isNotFound());

        assertEquals("PERMITIDO", unicaFila().getResultado());
    }

    /** Sin codigo de estudiante en la ruta no hay a quien atribuirle la consulta. */
    @Test
    void unCodigoConFormaInvalidaNoSeAtribuyeANingunEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "codigo-larguisimo-invalido")
                        .with(comoEstudiante("A00123456")))
                .andExpect(status().isForbidden());

        assertNull(unicaFila().getEstudianteConsultado());
    }

    @Test
    void elHistorialSeConsultaPorEstudiante() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00123456")));
        mockMvc.perform(get(RUTA, "A00123456").with(comoEstudiante("A00987654")));

        List<AuditoriaAcceso> accesos =
                auditoria.findByEstudianteConsultadoOrderByOcurridoEnDesc("A00123456");
        assertEquals(2, accesos.size());
    }
}
