package co.edu.icesi.vista360.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Escribe el 401 y el 403 en problem+json.
 */
@Component
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper json;

    RespuestasDeSeguridad(ObjectProvider<ObjectMapper> disponible) {
        this.json = disponible.getIfAvailable(ObjectMapper::new);
    }

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
            org.springframework.security.core.AuthenticationException fallo) throws IOException {
        escribir(peticion, respuesta, HttpStatus.UNAUTHORIZED,
                "Falta un token válido de la plataforma de identidad");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
            org.springframework.security.access.AccessDeniedException fallo) throws IOException {
        escribir(peticion, respuesta, HttpStatus.FORBIDDEN,
                "El token no autoriza a consultar la información de ese estudiante");
    }
    private void escribir(HttpServletRequest peticion, HttpServletResponse respuesta,
            HttpStatus estado, String detalle) throws IOException {
        Map<String, Object> problema = new LinkedHashMap<>();
        problema.put("title", estado.getReasonPhrase());
        problema.put("status", estado.value());
        problema.put("detail", detalle);
        problema.put("instance", peticion.getRequestURI());

        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        json.writeValue(respuesta.getWriter(), problema);
    }
}
