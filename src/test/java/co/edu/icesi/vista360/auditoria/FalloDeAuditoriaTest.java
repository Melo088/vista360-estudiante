package co.edu.icesi.vista360.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Un fallo de auditoria no cambia la respuesta y tampoco pasa inadvertido. */
@SpringBootTest
@AutoConfigureMockMvc
class FalloDeAuditoriaTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistroDeAcceso registro;

    @Autowired
    private SaludDeAuditoria salud;

    @MockitoBean
    private AuditoriaAccesoRepository auditoria;

    @Test
    void laConsultaResponde200AunqueLaAuditoriaNoSePuedaEscribir() throws Exception {
        given(auditoria.save(any())).willThrow(new IllegalStateException("base caida"));
        long antes = registro.getFallos();

        mockMvc.perform(get(RUTA, "A00123456")
                        .with(jwt().jwt(t -> t.subject("A00123456").claim("rol", "ESTUDIANTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materias.length()").value(5));

        assertEquals(antes + 1, registro.getFallos(), "el fallo no quedo contado");
        assertEquals(Status.DOWN, salud.health().getStatus(),
                "la salud no reflejo la perdida de filas de auditoria");
    }
}
