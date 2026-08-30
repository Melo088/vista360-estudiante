package co.edu.icesi.vista360.matricula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica las dos promesas que el codigo hace y que no se ven en la respuesta: que la lectura
 * no degenera en un N+1 y que el respaldo del S-17 funciona fuera del periodo vigente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class MatriculaConsultasTest {

    private static final String RUTA = "/api/v1/estudiantes/{estudianteId}/matricula-actual";

    /** Una fecha de enero, entre semestres: ningun periodo de la semilla la contiene. */
    private static final Instant ENTRE_SEMESTRES = Instant.parse("2027-01-15T10:00:00Z");

    @TestConfiguration
    static class RelojFijo {

        // Otro nombre de bean que el de RelojConfig: dos definiciones con el mismo nombre
        // chocan, y @Primary solo decide a quien se inyecta cuando hay dos candidatos.
        @Bean
        @Primary
        Clock relojFijoDeEnero() {
            return Clock.fixed(ENTRE_SEMESTRES, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Clock reloj;

    @Autowired
    private EntityManagerFactory fabrica;

    /**
     * Lo que importa no es un numero fijo de consultas, sino que no dependa de cuantas
     * materias tenga el estudiante.
     */
    @Test
    void elNumeroDeConsultasNoCreceConLaCantidadDeMaterias() throws Exception {
        long conCincoMaterias = sentenciasDe("A00987654");
        long sinMaterias = sentenciasDe("A00555000");

        assertEquals(sinMaterias, conCincoMaterias,
                "cinco materias costaron " + (conCincoMaterias - sinMaterias)
                        + " sentencias mas que ninguna: hay un N+1");
        assertTrue(conCincoMaterias <= 4,
                "la consulta uso " + conCincoMaterias + " sentencias, mas de las esperadas");
    }

    /** Sentencias que dispara una consulta completa al endpoint. */
    private long sentenciasDe(String estudianteId) throws Exception {
        Statistics estadisticas = fabrica.unwrap(SessionFactory.class).getStatistics();
        estadisticas.clear();
        mockMvc.perform(get(RUTA, estudianteId)
                        .with(jwt().jwt(t -> t.subject(estudianteId).claim("rol", "ESTUDIANTE"))))
                .andExpect(status().isOk());
        return estadisticas.getPrepareStatementCount();
    }

    /**
     * Con el reloj en enero no hay periodo vigente, y el acompanante igual necesita ver el
     * ultimo periodo cursado del estudiante (S-17).
     */
    @Test
    void fueraDelPeriodoVigenteDevuelveElUltimoCursado() throws Exception {
        mockMvc.perform(get(RUTA, "A00123456").with(jwt().jwt(t -> t.subject("A00123456").claim("rol", "ESTUDIANTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo.codigo").value("202620"))
                .andExpect(jsonPath("$.materias.length()").value(5));
    }

    /** El reloj fijo tiene que estar puesto, o los dos tests de arriba no prueban nada. */
    @Test
    void elRelojDelContextoEstaEnEnero() {
        assertEquals(ENTRE_SEMESTRES, Instant.now(reloj));
    }

}
