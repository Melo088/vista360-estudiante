package co.edu.icesi.vista360.seguridad;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Emite tokens firmados para probar el servicio sin plataforma de identidad.
 */
@RestController
@RequestMapping("/dev")
@Profile("dev")
@Tag(name = "Desarrollo", description = "Solo bajo el perfil dev. No existe en produccion")
public class TokenDesarrolloController {

    private static final Duration VIGENCIA = Duration.ofHours(8);

    private final JwtEncoder codificador;
    private final Clock reloj;

    TokenDesarrolloController(JwtEncoder codificador, Clock reloj) {
        this.codificador = codificador;
        this.reloj = reloj;
    }

    @Operation(
            summary = "Emite un token de prueba",
            description = """
                    Reemplaza a la plataforma de identidad en local (S-10, S-18). El sujeto es el
                    código institucional si el rol es ESTUDIANTE, y el identificador del
                    acompañante si el rol es ACOMPANAMIENTO.""")
    @GetMapping("/token")
    public TokenEmitido token(@RequestParam String sujeto, @RequestParam String rol) {
        Instant ahora = Instant.now(reloj);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vista360-dev")
                .subject(sujeto)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .build();
        var parametros = JwtEncoderParameters.from(
                JwsHeader.with(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build(),
                claims);
        return new TokenEmitido(codificador.encode(parametros).getTokenValue(),
                ahora.plus(VIGENCIA));
    }

    /** Token listo para pegar en el botón Authorize de Swagger. */
    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
