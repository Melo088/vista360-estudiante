package co.edu.icesi.vista360.seguridad;

import co.edu.icesi.vista360.acompanamiento.AsignacionAcompanamientoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

/** Decide quien puede consultar a que estudiante, registrada una sola vez sobre la ruta. */
@Component
public class AutorizacionEstudiante
        implements AuthorizationManager<RequestAuthorizationContext> {

    /** Nombre de la variable de ruta que lleva el codigo institucional. */
    public static final String VARIABLE_RUTA = "estudianteId";

    /** Claim donde la plataforma de identidad declara el rol (S-18). */
    private static final String CLAIM_ROL = "rol";

    private final AsignacionAcompanamientoRepository asignaciones;
    private final Clock reloj;

    AutorizacionEstudiante(AsignacionAcompanamientoRepository asignaciones, Clock reloj) {
        this.asignaciones = asignaciones;
        this.reloj = reloj;
    }

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> autenticacion,
            RequestAuthorizationContext contexto) {

        String estudianteId = contexto.getVariables().get(VARIABLE_RUTA);
        Authentication quien = autenticacion.get();

        if (estudianteId == null || !(quien instanceof JwtAuthenticationToken token)) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(permite(token.getToken(), estudianteId));
    }

    private boolean permite(Jwt token, String estudianteId) {
        String sujeto = token.getSubject();
        if (sujeto == null) {
            return false;
        }
        return RolUsuario.desde(token.getClaimAsString(CLAIM_ROL))
                .map(rol -> switch (rol) {
                    case ESTUDIANTE -> sujeto.equals(estudianteId);
                    case ACOMPANAMIENTO ->
                            asignaciones.teniaAsignado(sujeto, estudianteId, LocalDate.now(reloj));
                })
                .orElse(false);
    }
}
