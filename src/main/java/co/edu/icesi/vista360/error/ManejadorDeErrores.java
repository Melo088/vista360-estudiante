package co.edu.icesi.vista360.error;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException fallo, HttpServletRequest peticion) {
        return problema(HttpStatus.NOT_FOUND, fallo.getMessage(), peticion);
    }

    private ProblemDetail problema(HttpStatus estado, String detalle, HttpServletRequest peticion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setInstance(URI.create(peticion.getRequestURI()));
        return problema;
    }
}
