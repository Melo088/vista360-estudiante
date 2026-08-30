package co.edu.icesi.vista360.auditoria;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra cada consulta a la informacion de un estudiante (Escenario 4B).
 */
public class RegistroDeAccesoFilter extends OncePerRequestFilter {

    /** Ruta del contrato que nombra a un estudiante. */
    private static final Pattern RUTA_ESTUDIANTE =
            Pattern.compile("^/api/v1/estudiantes/([^/]+)(/.*)?$");

    /** Atributo con el que el paso siguiente propagara la correlacion al log. */
    public static final String ATRIBUTO_CORRELACION = "vista360.correlacionId";

    private static final String CLAIM_ROL = "rol";
    private static final int LARGO_CODIGO_ESTUDIANTE = 9;

    private final RegistroDeAcceso registro;
    private final Clock reloj;

    public RegistroDeAccesoFilter(RegistroDeAcceso registro, Clock reloj) {
        this.registro = registro;
        this.reloj = reloj;
    }

    /** Solo se audita lo que toca informacion de un estudiante. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest peticion) {
        return !RUTA_ESTUDIANTE.matcher(peticion.getRequestURI()).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {

        String correlacionId = UUID.randomUUID().toString();
        peticion.setAttribute(ATRIBUTO_CORRELACION, correlacionId);
        Instant ocurridoEn = Instant.now(reloj);

        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            registro.registrar(armar(peticion, respuesta, ocurridoEn, correlacionId));
        }
    }

    private AuditoriaAcceso armar(HttpServletRequest peticion, HttpServletResponse respuesta,
            Instant ocurridoEn, String correlacionId) {

        Authentication quien = SecurityContextHolder.getContext().getAuthentication();
        String sujeto = AuditoriaAcceso.SUJETO_ANONIMO;
        String rol = AuditoriaAcceso.ROL_NINGUNO;
        if (quien instanceof JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();
            sujeto = recortar(jwt.getSubject(), 50, AuditoriaAcceso.SUJETO_ANONIMO);
            rol = recortar(jwt.getClaimAsString(CLAIM_ROL), 30, AuditoriaAcceso.ROL_NINGUNO);
        }

        return new AuditoriaAcceso(ocurridoEn, sujeto, "PERSONA", rol,
                estudianteDeLaRuta(peticion.getRequestURI()),
                recortar(peticion.getRequestURI(), 200, "/"),
                recortar(peticion.getMethod(), 10, "GET"),
                resultado(respuesta.getStatus()),
                recortar(peticion.getRemoteAddr(), 45, null),
                recortar(peticion.getHeader("User-Agent"), 300, null),
                correlacionId);
    }

    /** Una consulta autorizada sobre un estudiante inexistente queda como permitida. */
    private static String resultado(int estado) {
        return estado == 401 || estado == 403 ? "DENEGADO" : "PERMITIDO";
    }

    /** Nulo si lo que viene en la ruta no tiene forma de codigo institucional. */
    private static String estudianteDeLaRuta(String uri) {
        Matcher coincidencia = RUTA_ESTUDIANTE.matcher(uri);
        if (!coincidencia.matches()) {
            return null;
        }
        String candidato = coincidencia.group(1);
        return candidato.length() <= LARGO_CODIGO_ESTUDIANTE ? candidato : null;
    }

    private static String recortar(String valor, int largo, String siFalta) {
        if (valor == null || valor.isBlank()) {
            return siFalta;
        }
        return valor.length() <= largo ? valor : valor.substring(0, largo);
    }
}
